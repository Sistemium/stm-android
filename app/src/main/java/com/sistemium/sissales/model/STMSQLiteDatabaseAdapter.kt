package com.sistemium.sissales.model

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.base.STMConstants
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

    val operationPoolQueue = Executors.newFixedThreadPool(STMConstants.POOL_SIZE)

    val operationQueue = Executors.newFixedThreadPool(1)

    var database: SQLiteDatabase? = null

    val poolDatabases = arrayListOf<SQLiteDatabase>()

    override var storageType = STMStorageType.STMStorageTypeSQLiteDatabase

    override var builtInAttributeNames: Array<String> = arrayOf(
    STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY,
    STMConstants.STMPersistingKeyCreationTimestamp,
    STMConstants.STMPersistingKeyVersion,
    STMConstants.STMPersistingOptionLts,
    STMConstants.STMPersistingKeyPhantom
    )

    override var ignoredAttributeNames: Array<String> = builtInAttributeNames.plus("xid")

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

        }

        return operation.transaction

    }

    override fun endTransactionWithSuccess(transaction: STMPersistingTransaction, success: Boolean) {

        val operation = (transaction as STMSQLiteDatabaseTransaction).operation

        operation!!.success = success

        operation.finish()

    }

    private fun checkModelMapping(){

        val schema = STMSQLIteSchema()

        val savedModelPath = dbPath.replace(".db", ".json")

        val destinationModel = model.managedObjectModel

        var savedModel:STMManagedObjectModel? = null

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

            schema.createTablesWithModelMapping(modelMapper)

            modelMapper.destinationModel.saveToFile(savedModelPath)

        }

    }

}