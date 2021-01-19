package com.sistemium.sissales.base.session

import android.os.StrictMode
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.base.classes.entitycontrollers.STMClientDataController
import com.sistemium.sissales.base.classes.entitycontrollers.STMEntityController
import com.sistemium.sissales.base.classes.entitycontrollers.STMRecordStatusController
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMFullStackPersisting
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.interfaces.STMPersistingMergeInterceptor
import com.sistemium.sissales.interfaces.STMSettingsController
import com.sistemium.sissales.model.STMModeller
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter
import com.sistemium.sissales.persisting.STMPersister
import com.sistemium.sissales.persisting.STMPersisterFantoms
import com.sistemium.sissales.persisting.STMPersisterRunner
import com.sistemium.sissales.persisting.STMPersistingInterceptorUniqueProperty
import java.io.File
import java.util.*


/**
 * Created by edgarjanvuicik on 08/02/2018.
 */
class STMSession {

    companion object {

        private var INSTANCE:STMSession? = null

        var sharedSession: STMSession?
            get() {

                if (INSTANCE == null){

                    INSTANCE = STMSession()

                }

                return INSTANCE

            }
            set(value) {

                INSTANCE = value

            }
    }

    var persistenceDelegate: STMFullStackPersisting
    var logger: STMLogger? = null
    var settingsController: STMSettingsController? = null
    var syncer: STMSyncer? = null

    init {

        val dataModelName = STMCoreAuthController.dataModelName

        val databaseFile = "$dataModelName.db"

        val databasePath = STMCoreSessionFiler.sharedSession!!.persistencePath(STMConstants.SQL_LITE_PATH) + "/" + databaseFile

        val header:Map<String,String>? = if (STMCoreAuthController.modelEtag != null) mapOf("if-none-match" to STMCoreAuthController.modelEtag!!) else null

        var newModel = ""

        val savedModelPath = databasePath.replace(".db", ".json")

        val file = File(savedModelPath)

        if (file.exists()) {

            val stream = file.inputStream()

            val scanner = Scanner(stream)

            val jsonModelString = StringBuilder()

            while (scanner.hasNext()) {
                jsonModelString.append(scanner.nextLine())
            }

            newModel = jsonModelString.toString()

        }

        var path = "https://api.sistemium.com/models/i${STMCoreAuthController.configuration}.json"

        if (STMCoreAuthController.configuration.contains("vfs")){

            path = "https://api.sistemium.com/models/${STMCoreAuthController.configuration}.json"

        }

        //allow strict mode to fix network on main thread exception
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)

        val (_, response, result) = Fuel.get(path)
                .header(header)
                .responseJson()

        when (result) {
            is Result.Success -> {

                newModel = result.get().content

                STMCoreAuthController.modelEtag = response.headers["ETag"]?.first()

            }

        }

        if (newModel == ""){

            throw Exception("No model response")

        }

        STMModelling.sharedModeler = STMModeller(newModel)

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
        syncerHelper.dataDownloadingOwner = syncer
        syncer.dataDownloadingDelegate = syncerHelper

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