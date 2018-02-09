package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.calsses.entitycontrollers.STMRecordStatusController
import com.sistemium.sissales.enums.STMSessionStatus
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.*
import com.sistemium.sissales.model.STMModeller
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter
import com.sistemium.sissales.persisting.STMPersister
import com.sistemium.sissales.persisting.STMPersisterRunner
import com.sistemium.sissales.persisting.STMPersistingInterceptorUniqueProperty

/**
 * Created by edgarjanvuicik on 08/02/2018.
 */
class STMCoreSession(var trackers: ArrayList<String>, var startSettings: Map<String, String>) :STMSession {
    override var coreSettingsController: STMCoreSettingsController? = null

    var manager: STMSessionManager? = null
    var startTrackers:ArrayList<String> = ArrayList(trackers)
    override var uid:String = STMCoreAuthController.userID!!
    override var filing: STMFiling = STMCoreSessionFiler(STMCoreAuthController.accountOrg!!, STMCoreAuthController.iSisDB ?: uid)
    override var status: STMSessionStatus = STMSessionStatus.STMSessionStarting
    override var persistenceDelegate: STMFullStackPersisting = initPersistable()
    var settingsController: STMSettingsController? = null

    fun initPersistable():STMPersister{

        var dataModelName = startSettings["dataModelName"]

        if (dataModelName == null){

            dataModelName = STMCoreAuthController.dataModelName

        }

        val databaseFile = dataModelName + ".db"

        val databasePath = filing.persistencePath(STMConstants.SQL_LITE_PATH) + "/" + databaseFile

        val modeler = STMModeller(filing.bundledModelJSON(dataModelName))

        val adapter = STMSQLiteDatabaseAdapter(modeler, databasePath)

        val runner = STMPersisterRunner(hashMapOf(STMStorageType.STMStorageTypeSQLiteDatabase to adapter))

        val persister = STMPersister(runner)

        val entityNameInterceptor = STMPersistingInterceptorUniqueProperty()
        entityNameInterceptor.entityName = STMConstants.STM_ENTITY_NAME
        entityNameInterceptor.propertyName = "name"

        persister.beforeMergeEntityName(entityNameInterceptor.entityName!!, entityNameInterceptor)

        val recordStatusInterceptor = STMRecordStatusController()

        persister.beforeMergeEntityName(STMConstants.STM_RECORDSTATUS_NAME, recordStatusInterceptor)

        persistenceDelegate = persister

        modeler.persistanceDelegate = persistenceDelegate

//        settingsController = STMCoreSettingsController(startSettings, defaultSettings)
//        self.settingsController.persistenceDelegate = self.persistenceDelegate;
//        self.settingsController.session = self;
//        self.controllers[NSStringFromClass([self settingsControllerClass])] = self.settingsController;
//        val settingsInterceptor = STMCoreSettingsController()
//        persister.beforeMergeEntityName(STMConstants.STM_SETTING_NAME, settingsInterceptor)
//        self.logger = [STMLogger sharedLogger];
//        self.logger.session = self;

        trackers = ArrayList()

//        [self checkTrackersToStart];

        status = STMSessionStatus.STMSessionRunning

//        [self setupSyncer];

        return persister

    }

    fun stopSession(){

        TODO("not implemented")

    }

    fun dismissSession(){

        TODO("not implemented")

    }

}