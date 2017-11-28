package com.sistemium.sissales.model

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import com.sistemium.sissales.persisting.STMSQLiteDatabaseOperation
import java.util.concurrent.Executors

/**
 * Created by edgarjanvuicik on 15/11/2017.
 */
class STMSQLiteDatabaseAdapter(override var model: STMModelling, dbPath:String) :STMAdapting {

    val operationPoolQueue = Executors.newFixedThreadPool(STMConstants.POOL_SIZE)

    val operationQueue = Executors.newFixedThreadPool(1)

    var database: SQLiteDatabase? = null

    val poolDatabases = arrayListOf<SQLiteDatabase>()

    override var storageType = STMStorageType.STMStorageTypeSQLiteDatabase

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

        operation.waitUntilTransactionIsReady()

        TODO("not implemented")

//        return operation.transaction

    }

    private fun checkModelMapping(){

        TODO("not implemented")

    }

}