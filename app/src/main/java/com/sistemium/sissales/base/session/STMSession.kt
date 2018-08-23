package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.classes.entitycontrollers.STMClientDataController
import com.sistemium.sissales.base.classes.entitycontrollers.STMEntityController
import com.sistemium.sissales.base.classes.entitycontrollers.STMRecordStatusController
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
class STMSession {

    companion object {

        private var INSTANCE:STMSession? = null

        var sharedSession: STMSession?
            get() {

                if (INSTANCE == null){

                    INSTANCE = try {STMSession()} catch (e:Exception) { null }

                }

                return INSTANCE

            }
            set(value) {

                INSTANCE = value

            }
    }

    var uid: String = STMCoreAuthController.userID!!
    var filing: STMFiling = STMCoreSessionFiler(STMCoreAuthController.accountOrg!!, STMCoreAuthController.iSisDB
            ?: uid)
    var persistenceDelegate: STMFullStackPersisting
    var logger: STMLogger? = null
    var settingsController: STMSettingsController? = null
    var syncer: STMSyncer? = null

    init {

        val dataModelName = STMCoreAuthController.dataModelName

        val databaseFile = "$dataModelName.db"

        val databasePath = filing.persistencePath(STMConstants.SQL_LITE_PATH) + "/" + databaseFile

        STMModelling.sharedModeler = STMModeller(filing.bundledModelJSON(dataModelName))

        val adapter = STMSQLiteDatabaseAdapter(databasePath)

        val runner = STMPersisterRunner(hashMapOf(STMStorageType.STMStorageTypeSQLiteDatabase to adapter))

        val persister = STMPersister(runner)

        val entityNameInterceptor = STMPersistingInterceptorUniqueProperty()
        entityNameInterceptor.entityName = STMConstants.STM_ENTITY_NAME
        entityNameInterceptor.propertyName = "name"

        persister.beforeMergeEntityName(entityNameInterceptor.entityName!!, entityNameInterceptor)

        val recordStatusInterceptor = STMRecordStatusController()

        persister.beforeMergeEntityName(STMConstants.STM_RECORDSTATUS_NAME, recordStatusInterceptor)

        this.persistenceDelegate = persister

        val settings = STMCoreSettingsController()
        settings.persistenceDelegate = persistenceDelegate
        STMClientDataController.persistenceDelegate = persistenceDelegate
        settingsController = settings
        val settingsInterceptor = settingsController as STMPersistingMergeInterceptor
        persister.beforeMergeEntityName(STMConstants.STM_SETTING_NAME, settingsInterceptor)

        logger = STMLogger.sharedLogger
        logger?.session = this

    }

    fun setupSyncer() {

        val syncer = STMSyncer()
        val syncerHelper = STMSyncerHelper()
        syncerHelper.persistenceFantomsDelegate = STMPersisterFantoms()
        syncerHelper.dataDownloadingOwner = syncer
        syncerHelper.defantomizingOwner = syncer
        syncer.dataDownloadingDelegate = syncerHelper
        syncer.defantomizingDelegate = syncerHelper

        val unsyncedHelper = STMUnsyncedDataHelper()
        unsyncedHelper.session = this
        STMEntityController.sharedInstance!!.owner = syncer
        unsyncedHelper.subscriberDelegate = syncer
        syncer.dataSyncingDelegate = unsyncedHelper
        syncer.session = this
        this.syncer = syncer
        syncer.startSyncer()

    }

}