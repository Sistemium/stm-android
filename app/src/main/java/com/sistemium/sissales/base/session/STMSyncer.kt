package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.calsses.entitycontrollers.STMEntityController
import com.sistemium.sissales.enums.STMSocketEvent
import com.sistemium.sissales.interfaces.*
import java.util.*

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMSyncer: STMDefantomizingOwner, STMDataDownloadingOwner, STMDataSyncingSubscriber, STMSocketConnectionOwner, STMRemoteDataEventHandling {

    var dataDownloadingDelegate: STMDataDownloading? = null
    var defantomizingDelegate: STMDefantomizing? = null
    var dataSyncingDelegate:STMDataSyncing? = null
    var session:STMSession? = null
    var isRunning = false
    var socketTransport:STMSocketConnection? = null
    var isSendingData = false
    var syncTimer: Timer? = null
    var needRepeatDownload = false

    private var _settings:Map<*,*>? = null

    var settings:Map<*,*>? = null
    get() {

        if (_settings == null) {

            _settings = session?.settingsController?.currentSettingsForGroup("syncer")

        }

        return _settings

    }


    fun startSyncer(){

        if (isRunning) return

        _settings = null

        if (!checkStcEntities()){

            session?.logger?.errorMessage("checkStcEntities fail")

        }

        if (STMCoreAuthController.socketURL == null){

            session?.logger?.errorMessage("Syncer has no socketURL")

            return

        }

        STMEntityController.sharedInstance.checkEntitiesForDuplicates()

        //TODO client data
//        STMClientDataController.checkClientData()

        session?.logger?.infoMessage("Syncer start")

        socketTransport = STMSocketTransport(STMCoreAuthController.socketURL!!, STMCoreAuthController.entityResource!!, this, this)

        isRunning = true

    }

    fun checkStcEntities():Boolean{

        STMEntityController.sharedInstance.persistenceDelegate = session?.persistenceDelegate

        val stcEntities = STMEntityController.sharedInstance.stcEntities

        val entity = stcEntities?.get("STMEntity")

        if (entity != null){

            if (STMCoreAuthController.entityResource == null) {

                session?.logger?.errorMessage("ERROR! syncer have no settings, something really wrong here, needs attention!")
                return false

            }

            val attributes = hashMapOf(
                    "name" to STMFunctions.removePrefixFromEntityName("STMEntity"),
                    "url" to STMCoreAuthController.entityResource
            )

            session?.persistenceDelegate?.mergeSync("STMEntity", attributes, hashMapOf(STMConstants.STMPersistingOptionLts to STMFunctions.stringFrom(Date())))


        } else if (entity?.get("url") != null && entity!!["url"] != STMCoreAuthController.entityResource) {

            STMFunctions.debugLog("STMSyncer", "change STMEntity url from ${entity!!["url"]} to ${STMCoreAuthController.entityResource}")

            val attributes = HashMap(entity!! as Map)

            attributes["url"] = STMCoreAuthController.entityResource

            session?.persistenceDelegate?.mergeSync("STMEntity", attributes, hashMapOf(STMConstants.STMPersistingOptionLts to STMFunctions.stringFrom(Date())))

        }

        return true

    }

    fun sendEventViaSocket(event:STMSocketEvent, value:Any){

        socketTransport?.socketSendEvent(event, value)

    }

    override fun socketReceiveAuthorization(){

        subscribeToUnsyncedObjects()

        initTimer()

        val downloadableEntityNames = STMEntityController.sharedInstance.downloadableEntityNames()
        val downloadableEntityResources = downloadableEntityNames.map {

            STMEntityController.sharedInstance.resourceForEntity(it)

        }

        socketTransport!!.socketSendEvent(STMSocketEvent.STMSocketEventSubscribe, downloadableEntityResources)

    }

    private fun subscribeToUnsyncedObjects(){

        unsubscribeFromUnsyncedObjects()
        dataSyncingDelegate!!.subscriberDelegate = this
        dataSyncingDelegate!!.startSyncing()

    }

    private fun unsubscribeFromUnsyncedObjects(){

        dataSyncingDelegate!!.subscriberDelegate = null
        dataSyncingDelegate!!.pauseSyncing()
        finishUnsyncedProcess()

    }

    override fun finishUnsyncedProcess(){

        isSendingData = false

    }

    private fun initTimer(){

        syncTimer?.cancel()

        syncTimer = Timer()

        val syncInterval:Int = settings?.get("syncInterval") as? Int ?: STMConstants.syncInterval

        syncTimer!!.schedule(object : TimerTask(){

            override fun run() {

                if (socketTransport?.isReady == true) {

                    receiveData()

                }

            }

        }, 0, syncInterval.toLong() * 1000)

    }

    fun receiveData(){

        if (!isRunning) return

        STMLogger.sharedLogger.infoMessage("STMSyncer")

        if (dataDownloadingDelegate!!.downloadingQueue != null){

            needRepeatDownload = true

        }

        dataDownloadingDelegate!!.startDownloading(null)

    }

    override fun haveUnsynced(entityName: String, itemData: Map<*, *>, itemVersion: String) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}