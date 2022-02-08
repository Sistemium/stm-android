package com.sistemium.sissales.model

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import com.sistemium.sissales.persisting.STMSQLiteDatabaseOperation
import com.sistemium.sissales.persisting.STMSQLiteDatabaseTransaction
import java.io.File
import java.util.*
import java.util.concurrent.Executors

/**
 * Created by edgarjanvuicik on 15/11/2017.
 */
class STMSQLiteDatabaseAdapter(private var dbPath: String) : STMAdapting {

    override var storageType = STMStorageType.STMStorageTypeSQLiteDatabase

    override var builtInAttributeNames: Array<String> = arrayOf(
            STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY,
            STMConstants.STMPersistingKeyCreationTimestamp,
            STMConstants.STMPersistingKeyVersion,
            STMConstants.STMPersistingOptionLts,
            STMConstants.STMPersistingKeyPhantom
    )

    override var columnsByTable: Map<String, Map<String, String>>? = null

    override var ignoredAttributeNames: Array<String> = builtInAttributeNames.plus("xid")

    var database: SQLiteDatabase? = null

    val poolDatabases = arrayListOf<SQLiteDatabase>()

    private val operationQueue = Executors.newFixedThreadPool(1)

    private val operationPoolQueue = Executors.newFixedThreadPool(STMConstants.POOL_SIZE)

    init {

        val flags = SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY or SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING

        database = SQLiteDatabase.openDatabase(dbPath, null, flags)

        database!!.execSQL("PRAGMA TEMP_STORE=MEMORY;")

        database!!.enableWriteAheadLogging()

        checkModelMapping()

        for (i in 0 until STMConstants.POOL_SIZE) {

            val poolDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

            poolDatabases.add(poolDb)

        }

        if (poolDatabases.count() == 0) {

            poolDatabases.add(database!!)

        }

    }

    override fun beginTransaction(readOnly: Boolean): STMPersistingTransaction {

        val operation = STMSQLiteDatabaseOperation(readOnly, this)

        val transaction: STMPersistingTransaction

        if (readOnly) {

            operationPoolQueue.execute(operation)

            transaction = operation.transaction

//            STMFunctions.debugLog("Syncer", "did not beginTransactionNonExclusive")

        } else {

            operationQueue.execute(operation)

            transaction = operation.transaction

            database!!.beginTransactionNonExclusive()

//            STMFunctions.debugLog("Syncer", "beginTransactionNonExclusive")

        }

        return transaction

    }

    override fun endTransactionWithSuccess(transaction: STMPersistingTransaction, success: Boolean) {

        val operation = (transaction as STMSQLiteDatabaseTransaction).operation

        operation!!.success = success

        if (!operation.readOnly) {

            if (operation.success) {

                database!!.setTransactionSuccessful()

            } else {

                STMFunctions.debugLog("", "")

                TODO("not implemented")

            }

//            STMFunctions.debugLog("Syncer", "endTransaction")

            database!!.endTransaction()

        } else {

//            STMFunctions.debugLog("Syncer", "did not endTransaction")

        }

        operation.finish()

    }

    override fun close() {
        database?.close()
    }

    private fun checkModelMapping() {

        val _columnsByTable: Map<String, ArrayList<String>>?

        val schema = STMSQLIteSchema(database!!)

        val destinationModel = STMModelling.sharedModeler!!.managedObjectModel

        var savedModel: STMManagedObjectModel? = null

        val savedModelPath = dbPath.replace(".db", ".json")

        val file = File(savedModelPath)

        if (file.exists()) {

            val stream = file.inputStream()

            val scanner = Scanner(stream)

            val jsonModelString = StringBuilder()

            while (scanner.hasNext()) {
                jsonModelString.append(scanner.nextLine())
            }

            savedModel = STMManagedObjectModel(jsonModelString.toString())

        }

        val modelMapper = STMModelMapper(savedModel, destinationModel)

        if (modelMapper.needToMigrate) {

            schema.createTablesWithModelMapping(modelMapper, STMModelling.sharedModeler!!)

            modelMapper.destinationModel.saveToFile(savedModelPath)

        }

        _columnsByTable = schema.currentDBScheme()

        val columnsByTableWithTypes = hashMapOf<String, Map<String, String>>()

        for (tablename in _columnsByTable.keys) {

            val columns = hashMapOf<String, String>()

            for (columnname in _columnsByTable[tablename]!!) {

                val attributeType = STMModelling.sharedModeler!!.fieldsForEntityName(STMFunctions.addPrefixToEntityName(tablename))[columnname]?.attributeType

                columns[columnname] = attributeType ?: "Undefined"

            }

            columnsByTableWithTypes[tablename] = columns

        }

        columnsByTable = columnsByTableWithTypes

    }

}