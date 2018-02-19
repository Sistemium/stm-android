package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.calsses.entitycontrollers.STMEntityController
import com.sistemium.sissales.interfaces.*

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

            session?.persistenceDelegate?.mergeSync("STMEntity", attributes, hashMapOf(STMConstants.STMPersistingOptionLts to STMFunctions.stringFromNow()))


        } else if (entity?.get("url") != null && entity!!["url"] != STMCoreAuthController.entityResource) {

            STMFunctions.debugLog("STMSyncer", "change STMEntity url from ${entity!!["url"]} to ${STMCoreAuthController.entityResource}")

            val attributes = HashMap(entity!! as Map)

            attributes["url"] = STMCoreAuthController.entityResource

            session?.persistenceDelegate?.mergeSync("STMEntity", attributes, hashMapOf(STMConstants.STMPersistingOptionLts to STMFunctions.stringFromNow()))

        }

        return true

    }


}