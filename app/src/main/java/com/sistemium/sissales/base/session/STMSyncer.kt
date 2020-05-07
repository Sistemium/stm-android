package com.sistemium.sissales.base.session

import com.sistemium.sissales.activities.WebViewActivity
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMConstants.Companion.NOTIFICATION_SYNCER_SENDIG_DATA
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.classes.entitycontrollers.STMClientDataController
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePicturesController
import com.sistemium.sissales.base.classes.entitycontrollers.STMEntityController
import com.sistemium.sissales.enums.STMSocketEvent
import com.sistemium.sissales.interfaces.*
import nl.komponents.kovenant.then
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMSyncer : STMDefantomizingOwner, STMDataDownloadingOwner, STMDataSyncingSubscriber, STMSocketConnectionOwner, STMRemoteDataEventHandling {

    var dataDownloadingDelegate: STMDataDownloading? = null
    var defantomizingDelegate: STMDefantomizing? = null
    var dataSyncingDelegate: STMDataSyncing? = null
    var session: STMSession? = null
    private var needRepeatDownload = false
    private var isRunning = false
    var socketTransport: STMSocketConnection? = null
    private var isSendingData = false
    set(value) {

        if (value){

            WebViewActivity.webInterface?.postJSNotification(NOTIFICATION_SYNCER_SENDIG_DATA)

        }

        field = value

    }
    private var syncTimer: Timer? = null
    private var settings: Map<*, *>? = null
        get() {

            if (field == null) {

                field = session?.settingsController?.currentSettingsForGroup("syncer")

            }

            return field

        }
    private var isDefantomizing = false

    fun prepareToDestroy(){

        unsubscribeFromUnsyncedObjects()

        if (isRunning) {

            socketTransport?.closeSocket()

            releaseTimer()

            isRunning = false

        }


    }

    override fun socketReceiveAuthorization() {

        subscribeToUnsyncedObjects()

        initTimer()

        val downloadableEntityNames = STMEntityController.sharedInstance!!.downloadableEntityNames()
        val downloadableEntityResources = downloadableEntityNames.map {

            STMEntityController.sharedInstance!!.resourceForEntity(it)

        }

        socketTransport!!.socketSendEvent(STMSocketEvent.STMSocketEventSubscribe, downloadableEntityResources)

    }

    override fun finishUnsyncedProcess() {

        isSendingData = false

    }

    override fun haveUnsynced(entityName: String, itemData: Map<*, *>, itemVersion: String) {

        isSendingData = true
        socketTransport!!.mergeAsync(entityName, itemData, null)
                .then {

                    if (it["data"] == null) {

                        STMFunctions.debugLog("STMSyncer", "updateResource error: $it")

                    }

                    dataSyncingDelegate!!.setSynced(true, entityName, it["data"] as Map<*, *>, itemVersion)

                }.fail {

                    STMFunctions.debugLog("STMSyncer", "updateResource error: $it")

                    checkGoneEntity(entityName, it, itemData["id"].toString())

                    dataSyncingDelegate!!.setSynced(false, entityName, itemData, itemVersion)

                }

    }

    fun checkGoneEntity(entityName:String, e:Exception, id:String){

        if (e.localizedMessage == "410"){

            STMFunctions.debugLog("STMSyncer","destroy gone entity name: $entityName, id: $id")

            val options = hashMapOf(
                    STMConstants.STMPersistingOptionRecordstatuses to false
            )

            session!!.persistenceDelegate.destroy(entityName, id, options).fail {

                STMLogger.sharedLogger!!.errorMessage("Error destroy gone entity: ${it.localizedMessage}")

            }

        }

    }

    override fun receiveData(entityName: String, offset: String) {

        if (!socketTransport!!.isReady) {

            dataDownloadingDelegate!!.stopDownloading()

        }

        val fetchLimit = (settings!!["fetchLimit"] as? String)?.toInt() ?: STMConstants.fetchLimit

        val options = hashMapOf(
                STMConstants.STMPersistingOptionPageSize to fetchLimit,
                STMConstants.STMPersistingOptionOffset to offset
        )

        socketTransport!!.findAllAsync(entityName, options, null)
                .then {

                    val responseOffset = it[STMConstants.STMPersistingOptionOffset] as? String

                    val pageSize = (it[STMConstants.STMPersistingOptionPageSize] as Double).toInt()

                    dataDownloadingDelegate!!.dataReceivedSuccessfully(entityName, it["data"] as ArrayList<*>, responseOffset, pageSize, null)

                }.fail {

                    dataDownloadingDelegate!!.dataReceivedSuccessfully(entityName, null, null, null, it)

                }

    }

    override fun dataDownloadingFinished() {

        if (needRepeatDownload) {

            STMFunctions.debugLog("STMSyncer", "dataDownloadingFinished and needRepeatDownload")
            needRepeatDownload = false
            return receiveData()

        }

        STMFunctions.debugLog("STMSyncer", "dataDownloadingFinished")

        STMCorePicturesController.sharedInstance!!.checkNotUploadedPhotos()

        STMLogger.sharedLogger!!.infoMessage("dataDownloadingFinished")

        startDefantomization()

    }

    override fun defantomizingFinished() {

        isDefantomizing = false

    }

    override fun entitiesChanged() {

        subscribeToUnsyncedObjects()
        receiveData()

    }

    override fun defantomizeEntityName(entityName: String, identifier: String) {

        if (!isDefantomizing) return defantomizingDelegate!!.stopDefantomization()
        socketTransport!!.findAllAsync(entityName, null, identifier)
                .then {

                    if (it["error"]?.toString() != null) {

                        throw Exception(it["error"].toString())

                    }

                    defantomizingDelegate!!.defantomizedEntityName(entityName, identifier, it["data"] as Map<*, *>, null)

                }
                .fail {

                    defantomizingDelegate!!.defantomizedEntityName(entityName, identifier, null, it)

                }

    }

    override fun socketWillClosed() {

        stopSyncerActivity()

    }

    override fun remoteUpdated(entityName: String, attributes: Map<*, *>) {

        STMFunctions.debugLog("STMSyncer", "remoteUpdated entity name: $entityName, id: ${attributes["id"]}")

        session!!.persistenceDelegate.mergeSync(entityName, attributes, hashMapOf(STMConstants.STMPersistingOptionLts to STMFunctions.stringFrom(Date())))

    }

    override fun remoteHasNewData(entityName: String) {

        STMFunctions.debugLog("STMSyncer", "remoteHasNewData for entity name: $entityName")

        receiveEntities(arrayListOf(entityName))

    }

    override fun remoteDestroyed(entityName: String, id: String) {

        STMFunctions.debugLog("STMSyncer", "remoteDestroyed entity name: $entityName, id: $id")
        val options = hashMapOf(STMConstants.STMPersistingOptionRecordstatuses to false)
        session!!.persistenceDelegate.destroySync(entityName, id, options)

    }

    fun startSyncer() {

        if (isRunning) return

        settings = null

        if (!checkStcEntities()) {

            session?.logger?.errorMessage("checkStcEntities fail")

        }

        if (STMCoreAuthController.socketURL == null) {

            session?.logger?.errorMessage("Syncer has no socketURL")

            return

        }

        STMEntityController.sharedInstance!!.checkEntitiesForDuplicates()

        STMClientDataController.checkClientData()

        session?.logger?.infoMessage("Syncer start")

        socketTransport = STMSocketTransport(STMCoreAuthController.socketURL!!, this, this)

        isRunning = true

    }

    fun sendEventViaSocket(event: STMSocketEvent, value: Any) {

        socketTransport?.socketSendEvent(event, value)

    }

    private fun checkStcEntities(): Boolean {

        STMEntityController.sharedInstance!!.persistenceDelegate = session?.persistenceDelegate

        val stcEntities = STMEntityController.sharedInstance!!.stcEntities

        val entity = stcEntities?.get("STMEntity")

        if (entity == null) {

            if (STMCoreAuthController.entityResource == null) {

                session?.logger?.errorMessage("ERROR! syncer have no settings, something really wrong here, needs attention!")
                return false

            }

            val attributes = hashMapOf(
                    "name" to STMFunctions.removePrefixFromEntityName("STMEntity"),
                    "url" to STMCoreAuthController.entityResource
            )

            session?.persistenceDelegate?.mergeSync("STMEntity", attributes, hashMapOf(STMConstants.STMPersistingOptionLts to STMFunctions.stringFrom(Date())))


        } else if (entity["url"] != null && entity["url"] != STMCoreAuthController.entityResource) {

            STMFunctions.debugLog("STMSyncer", "change STMEntity url from ${entity["url"]} to ${STMCoreAuthController.entityResource}")

            val attributes = HashMap(entity)

            attributes["url"] = STMCoreAuthController.entityResource

            session?.persistenceDelegate?.mergeSync("STMEntity", attributes, hashMapOf(STMConstants.STMPersistingOptionLts to STMFunctions.stringFrom(Date())))

        }

        return true

    }

    private fun subscribeToUnsyncedObjects() {

        unsubscribeFromUnsyncedObjects()
        dataSyncingDelegate!!.subscriberDelegate = this
        dataSyncingDelegate!!.startSyncing()

    }

    private fun unsubscribeFromUnsyncedObjects() {

        dataSyncingDelegate!!.subscriberDelegate = null
        dataSyncingDelegate!!.pauseSyncing()
        finishUnsyncedProcess()

    }

    private fun initTimer() {

        syncTimer?.cancel()

        syncTimer = Timer()

        val syncInterval: Int = settings?.get("syncInterval") as? Int ?: STMConstants.syncInterval

        syncTimer!!.schedule(object : TimerTask() {

            override fun run() {

                if (socketTransport?.isReady == true) {

                    receiveData()

                }

            }

        }, 0, syncInterval.toLong() * 1000)

    }

    private fun releaseTimer() {

        syncTimer?.cancel()

        syncTimer = null

    }

    private fun receiveData() {

        if (!isRunning) return

        STMLogger.sharedLogger!!.infoMessage("STMSyncer")

        if (dataDownloadingDelegate!!.downloadingQueue != null) {

            needRepeatDownload = true

        }

        dataDownloadingDelegate!!.startDownloading(null)

    }

    private fun startDefantomization() {

        if (!socketTransport!!.isReady) {

            defantomizingDelegate!!.stopDefantomization()

        }

        if (isDefantomizing) {

            return

        }

        isDefantomizing = true

        defantomizingDelegate!!.startDefantomization()

    }

    private fun stopSyncerActivity() {

        if (dataSyncingDelegate!!.subscriberDelegate != null) {

            releaseTimer()
            unsubscribeFromUnsyncedObjects()
            isSendingData = false
            dataDownloadingDelegate!!.stopDownloading()
            defantomizingDelegate!!.stopDefantomization()

        }

    }

    private fun receiveEntities(entitiesNames: ArrayList<String>) {

        val existingNames = entitiesNames.map {

            STMFunctions.addPrefixToEntityName(it)

        }

        if (existingNames.isNotEmpty()) {

            dataDownloadingDelegate!!.startDownloading(ArrayList(existingNames))

        }

    }

}