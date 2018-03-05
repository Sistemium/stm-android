package com.sistemium.sissales.model

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
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
class STMSQLiteDatabaseAdapter(override var model: STMModelling, private var dbPath:String) :STMAdapting {

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

    private val operationPoolQueue = Executors.newFixedThreadPool(STMConstants.POOL_SIZE)

    private val operationQueue = Executors.newFixedThreadPool(1)

    init {

        val flags = SQLiteDatabase.OPEN_READWRITE or SQLiteDatabase.CREATE_IF_NECESSARY or SQLiteDatabase.ENABLE_WRITE_AHEAD_LOGGING

        database = SQLiteDatabase.openDatabase(dbPath, null, flags)

        database?.execSQL("PRAGMA TEMP_STORE=MEMORY;")

        checkModelMapping()

        for (i in 0 until STMConstants.POOL_SIZE){

            val poolDb = SQLiteDatabase.openDatabase(dbPath, null, SQLiteDatabase.OPEN_READONLY)

            poolDatabases.add(poolDb)

        }

    }

    override fun beginTransaction(readOnly:Boolean):STMPersistingTransaction{

        val operation = STMSQLiteDatabaseOperation(readOnly, this)

        if (readOnly){

            operationPoolQueue.execute(operation)

        }else{

            operationQueue.execute(operation)

            database!!.beginTransaction()

        }

        return operation.transaction

    }

    override fun endTransactionWithSuccess(transaction: STMPersistingTransaction, success: Boolean) {

        val operation = (transaction as STMSQLiteDatabaseTransaction).operation

        operation!!.success = success

        if (!operation.readOnly){

            if (operation.success){

                database!!.setTransactionSuccessful()

            }else{

                STMFunctions.debugLog("","")

                TODO("not implemented")

            }

            database!!.endTransaction()

        }

        operation.finish()



    }

    private fun checkModelMapping(){

        val _columnsByTable: Map<String, ArrayList<String>>?

        val schema = STMSQLIteSchema(database!!)

        val destinationModel = model.managedObjectModel

        var savedModel:STMManagedObjectModel? = null

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

            _columnsByTable = schema.createTablesWithModelMapping(modelMapper)

            if (columnsByTable != null){

                modelMapper.destinationModel.saveToFile(savedModelPath)

            }else{

                throw Exception("columnsByTable is null")

            }

        }else{

            _columnsByTable = schema.currentDBScheme()

        }

        val columnsByTableWithTypes = hashMapOf<String, Map<String, String>>()

        for (tablename in _columnsByTable.keys) {

            val columns = hashMapOf<String,String>()

            for (columnname in _columnsByTable[tablename]!!){

                val attributeType = model.fieldsForEntityName(STMFunctions.addPrefixToEntityName(tablename))[columnname]?.attributeType

                columns[columnname] = attributeType ?: "Undefined"

            }

            columnsByTableWithTypes[tablename] = columns

        }

        columnsByTable = columnsByTableWithTypes

    }

}