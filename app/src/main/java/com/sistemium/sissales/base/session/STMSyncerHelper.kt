package com.sistemium.sissales.base.session

import com.sistemium.sissales.activities.ProfileActivity
import com.sistemium.sissales.base.STMConstants.Companion.STMPersistingOptionLts
import com.sistemium.sissales.base.STMConstants.Companion.STM_ENTITY_NAME
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.classes.entitycontrollers.STMClientEntityController
import com.sistemium.sissales.base.classes.entitycontrollers.STMEntityController
import com.sistemium.sissales.interfaces.*
import nl.komponents.kovenant.then
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMSyncerHelper : STMDefantomizing, STMDataDownloading {

    override var downloadingQueue: ExecutorService? = null
    override var persistenceFantomsDelegate: STMPersistingFantoms? = null
    override var dataDownloadingOwner: STMDataDownloadingOwner? = null
    override var defantomizingOwner: STMDefantomizingOwner? = null
    private var downloadingOperations = ConcurrentHashMap<String, STMDownloadingOperation>()
    private var defantomizing: STMSyncerHelperDefantomizing? = null

    override fun startDownloading(entitiesNames: ArrayList<String>?) {

        var _entitiesNames = entitiesNames

        if (downloadingQueue != null) {

            return

        }

        downloadingQueue = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        if (_entitiesNames == null) {

            val names = hashSetOf(STM_ENTITY_NAME)
            names.addAll(STMEntityController.sharedInstance!!.downloadableEntityNames())

            if (STMEntityController.sharedInstance!!.entityWithName("STMSetting") != null) {

                names.add("STMSetting")

            }

            _entitiesNames = ArrayList(names)

        }

        ProfileActivity.profileActivityController?.setMaxProgress(_entitiesNames.size)

        for (entityName in _entitiesNames) {

            val operation = STMDownloadingOperation(entityName)

            operation.owner = this

            try {

                downloadingQueue?.execute(operation)

                downloadingOperations[entityName] = operation

            } catch (e: Exception) {

                STMFunctions.debugLog("STMSyncerHelper", "Rejecting execution")

            }

        }

    }

    override fun stopDownloading() {

        STMFunctions.debugLog("STMSyncerHelper", "stopDownloading")

        ProfileActivity.profileActivityController?.setProgressInfo(-1)

        downloadingQueue?.shutdown()

        for (operation in downloadingOperations.values) {

            operation.finish()

        }

        downloadingOperations.clear()

        dataDownloadingOwner?.dataDownloadingFinished()

    }

    override fun dataReceivedSuccessfully(entityName: String, dataRecieved: ArrayList<*>?, offset: String?, pageSize: Int?, error: Exception?) {

        if (error != null) {

            return doneDownloadingEntityName(entityName)

        }

        var currentEtag = STMClientEntityController.clientEntityWithName(entityName)["eTag"]

        if (currentEtag == null) {

            currentEtag = ""

        }

        if (dataRecieved!!.count() == 0 && offset!! != currentEtag) {

            STMClientEntityController.setEtag(entityName, offset)

        }

        if (dataRecieved.size == 0) {

            return doneDownloadingEntityName(entityName)

        }

        STMSession.sharedSession!!.persistenceDelegate.mergeMany(entityName, dataRecieved, hashMapOf(STMPersistingOptionLts to STMFunctions.stringFrom(Date())))
                .then {

                    findAllResultMergedWithSuccess(dataRecieved, entityName, offset!!, pageSize!!)

                }

                .fail {

                    doneDownloadingEntityName(entityName)

                }

    }

    override fun startDefantomization() {

        var defantomizing = this.defantomizing

        if (defantomizing == null) {

            defantomizing = STMSyncerHelperDefantomizing()

        }

        if (defantomizing.operations.count() != 0) return

        this.defantomizing = defantomizing

        for (entityName in STMEntityController.sharedInstance!!.entityNamesWithResolveFantoms()) {

            val entity = STMEntityController.sharedInstance!!.stcEntities!![entityName]

            if (entity?.get("url") == null) {

                STMFunctions.debugLog("STMSyncerHelper", "have no url for entity name: $entityName, fantoms will not to be resolved")
                continue
            }

            val results = persistenceFantomsDelegate!!.findAllFantomsIdsSync(entityName, defantomizing.failToResolveIds)

            if (results.count() == 0) continue
            STMFunctions.debugLog("STMSyncerHelper", "${results.count()} $entityName fantom(s)")
            for (identifier in results) {

                defantomizing.addDefantomizationOfEntityName(entityName, identifier)

            }

        }

        val count = defantomizing.operations.count()

        STMFunctions.debugLog("STMSyncerHelper", "DEFANTOMIZING_START with queue of $count")

        ProfileActivity.profileActivityController?.setMaxProgress(count)

        if (count == 0) defantomizingFinished()

    }

    override fun stopDefantomization() {

        defantomizing?.operationQueue?.shutdown()
        for (operation in defantomizing?.operations?.values ?: arrayListOf()) {

            operation.finish()

        }

        defantomizing?.operations?.clear()
        defantomizingFinished()

    }

    override fun defantomizedEntityName(entityName: String, identifier: String, attributes: Map<*, *>?, error: Exception?) {

        if (error != null) {

            if (!error.localizedMessage.startsWith("socket is not ready")) {

                STMFunctions.debugLog("STMSyncerHelper", "defantomize $entityName $identifier error: ${error.localizedMessage}")

                val deleteObject = error.localizedMessage.startsWith("403") || error.localizedMessage.startsWith("404")

                if (deleteObject) {

                    STMFunctions.debugLog("STMSyncerHelper", "delete fantom $entityName $identifier")
                    persistenceFantomsDelegate?.destroyFantomSync(entityName, identifier)

                } else {

                    TODO("not implemented")
//                @synchronized (self) {
//                    [self.defantomizing.failToResolveIds addObject:identifier];
//                }

                }

                doneWithEntityName(entityName, identifier)

            }

        } else {

            persistenceFantomsDelegate!!.mergeFantomAsync(entityName, attributes!!).then {

                doneWithEntityName(entityName, identifier)

            }

        }

    }

    private fun doneDownloadingEntityName(entityName: String) {

        val operation = downloadingOperations[entityName]

        downloadingOperations.remove(entityName)

        operation?.finish()

        STMFunctions.debugLog("STMSyncerHelper", "doneWith $entityName remain ${downloadingOperations.size} to receive")

        ProfileActivity.profileActivityController?.addProgress(1)

        if (downloadingOperations.size > 0) {

            return

        }

        downloadingQueue = null

        dataDownloadingOwner!!.dataDownloadingFinished()

    }

    private fun findAllResultMergedWithSuccess(result: ArrayList<*>, entityName: String, offset: String, pageSize: Int) {

        STMFunctions.debugLog("STMSyncerHelper", "    $entityName: got ${result.size} objects")

        ProfileActivity.profileActivityController?.setProgressInfo(result.size)

        if (result.size < pageSize) {

            STMClientEntityController.setEtag(entityName, offset)
            return doneDownloadingEntityName(entityName)

        }

        dataDownloadingOwner!!.receiveData(entityName, offset)

    }

    private fun defantomizingFinished() {

        STMFunctions.debugLog("STMSyncedHelper", "DEFANTOMIZING_FINISHED")
        ProfileActivity.profileActivityController?.setProgressInfo(-1)
        this.defantomizing = null
        defantomizingOwner!!.defantomizingFinished()

    }

    private fun doneWithEntityName(entityName: String, identifier: String) {


        defantomizing!!.operations[Pair(entityName, identifier)]!!.finish()

        defantomizing!!.operations.remove(Pair(entityName, identifier))

        val count = defantomizing!!.operations.size

        STMFunctions.debugLog("STMSyncerHelper", "doneWith $entityName $identifier ($count)")

        ProfileActivity.profileActivityController?.addProgress(1)

        if (count == 0) {

            startDefantomization()

        }

    }

}