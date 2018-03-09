package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.calsses.entitycontrollers.STMEntityController
import com.sistemium.sissales.calsses.entitycontrollers.STMRecordStatusController
import com.sistemium.sissales.enums.STMSessionStatus
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.*
import com.sistemium.sissales.model.STMModeller
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter
import com.sistemium.sissales.persisting.STMPersister
import com.sistemium.sissales.persisting.STMPersisterFantoms
import com.sistemium.sissales.persisting.STMPersisterRunner
import com.sistemium.sissales.persisting.STMPersistingInterceptorUniqueProperty

/**
 * Created by edgarjanvuicik on 08/02/2018.
 */
class STMCoreSession(var trackers: ArrayList<String>) :STMSession {

    override var uid:String = STMCoreAuthController.userID!!
    override var filing: STMFiling = STMCoreSessionFiler(STMCoreAuthController.accountOrg!!, STMCoreAuthController.iSisDB ?: uid)
    override var status: STMSessionStatus = STMSessionStatus.STMSessionStarting
    override lateinit var persistenceDelegate: STMFullStackPersisting
    override var logger: STMLogger? = null
    override var settingsController: STMSettingsController? = null
    override var syncer:STMSyncer? = null

    var manager: STMSessionManager? = null

    private var startTrackers:ArrayList<String> = ArrayList(trackers)

    init{

        val dataModelName = STMCoreAuthController.dataModelName

        val databaseFile = dataModelName + ".db"

        val databasePath = filing.persistencePath(STMConstants.SQL_LITE_PATH) + "/" + databaseFile

        val modeler = STMModeller(filing.bundledModelJSON(dataModelName))

        val adapter = STMSQLiteDatabaseAdapter(modeler, databasePath)

        MyApplication.testAdapter = adapter

        val runner = STMPersisterRunner(hashMapOf(STMStorageType.STMStorageTypeSQLiteDatabase to adapter))

        val persister = STMPersister(runner)

        val entityNameInterceptor = STMPersistingInterceptorUniqueProperty()
        entityNameInterceptor.entityName = STMConstants.STM_ENTITY_NAME
        entityNameInterceptor.propertyName = "name"

        persister.beforeMergeEntityName(entityNameInterceptor.entityName!!, entityNameInterceptor)

        val recordStatusInterceptor = STMRecordStatusController()

        persister.beforeMergeEntityName(STMConstants.STM_RECORDSTATUS_NAME, recordStatusInterceptor)

        this.persistenceDelegate = persister

        modeler.persistanceDelegate = persistenceDelegate

        val settings = STMCoreSettingsController()
        settings.persistenceDelegate = persistenceDelegate
        settingsController = settings
        val settingsInterceptor = settingsController as STMPersistingMergeInterceptor
        persister.beforeMergeEntityName(STMConstants.STM_SETTING_NAME, settingsInterceptor)

        logger = STMLogger.sharedLogger
        logger?.session = this

        trackers = ArrayList()

        checkTrackersToStart()

        status = STMSessionStatus.STMSessionRunning

        setupSyncer()

    }

    fun stopSession(){

        TODO("not implemented")

    }

    fun dismissSession(){

        TODO("not implemented")

    }

    private fun checkTrackersToStart(){

        //TODO check trackers
        if (startTrackers.contains("location")){

//            val locationTracker = STMCoreLocationTracker()
//            self.trackers[self.locationTracker.group] = self.locationTracker;
//            self.locationTracker.session = self;

        }

        if(startTrackers.contains("battery")){

//            self.batteryTracker = [[[self batteryTrackerClass] alloc] init];
//            self.trackers[self.batteryTracker.group] = self.batteryTracker;
//            self.batteryTracker.session = self;

        }

    }

    private fun setupSyncer(){

        val syncer = STMSyncer()
        val syncerHelper = STMSyncerHelper()
        syncerHelper.persistenceFantomsDelegate = STMPersisterFantoms()
        syncerHelper.dataDownloadingOwner = syncer
        syncerHelper.defantomizingOwner = syncer
        syncer.dataDownloadingDelegate = syncerHelper
        syncer.defantomizingDelegate = syncerHelper

        val unsyncedHelper = STMUnsyncedDataHelper()
        unsyncedHelper.session = this
        STMEntityController.sharedInstance.owner = syncer
        unsyncedHelper.subscriberDelegate = syncer
        syncer.dataSyncingDelegate = unsyncedHelper
        syncer.session = this
        this.syncer = syncer
        syncer.startSyncer()

    }

}