package com.sistemium.sissales.base.session

import com.sistemium.sissales.activities.ProfileActivity
import com.sistemium.sissales.base.STMConstants.Companion.STMPersistingOptionLts
import com.sistemium.sissales.base.STMConstants.Companion.STM_ENTITY_NAME
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.classes.entitycontrollers.STMClientEntityController
import com.sistemium.sissales.interfaces.*
import nl.komponents.kovenant.then
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

class STMSyncerHelper : STMDataDownloading {

    override var downloadingQueue: ExecutorService? = null
    override var dataDownloadingOwner: STMDataDownloadingOwner? = null
    private var downloadingOperations = ConcurrentHashMap<String, STMDownloadingOperation>()

    override fun startDownloading(entitiesNames: ArrayList<String>?) {

        var _entitiesNames = entitiesNames

        if (downloadingQueue != null) {

            return

        }

        downloadingQueue = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        if (_entitiesNames == null) {

            val names = hashSetOf(STM_ENTITY_NAME)

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

            return doneDownloadingEntityName(entityName, error.localizedMessage)

        }

        var currentEtag = STMClientEntityController.clientEntityWithName(entityName)["eTag"]

        if (currentEtag == null) {

            currentEtag = ""

        }

        if (dataRecieved!!.count() == 0 && offset!! != currentEtag) {

            STMClientEntityController.setEtag(entityName, offset)

        }

        if (dataRecieved.size == 0) {

            return doneDownloadingEntityName(entityName, null)

        }

        STMSession.sharedSession!!.persistenceDelegate.mergeMany(entityName, dataRecieved, hashMapOf(STMPersistingOptionLts to STMFunctions.stringFrom(Date())))
                .then {

                    findAllResultMergedWithSuccess(dataRecieved, entityName, offset!!, pageSize!!)

                }

                .fail {

                    doneDownloadingEntityName(entityName, it.localizedMessage)

                }

    }

    private fun doneDownloadingEntityName(entityName: String, errorMessage: String?) {

        if (errorMessage != null) {

            STMLogger.sharedLogger!!.errorMessage(errorMessage)

        }

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
            return doneDownloadingEntityName(entityName, null)

        }

        dataDownloadingOwner!!.receiveData(entityName, offset)

    }

}