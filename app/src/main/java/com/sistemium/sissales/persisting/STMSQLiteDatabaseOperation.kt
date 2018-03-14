package com.sistemium.sissales.persisting

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter

/**
 * Created by edgarjanvuicik on 24/11/2017.
 */

class STMSQLiteDatabaseOperation(val readOnly:Boolean, private var adapter: STMSQLiteDatabaseAdapter) :Runnable {

    var success = false

    val transaction:STMSQLiteDatabaseTransaction by lazy {

        if (_transaction != null) return@lazy _transaction!!

        synchronized(lock1){

            lock1.wait()

            return@lazy _transaction!!

        }

    }

    private val lock1 = Object()

    private val lock2 = Object()

    private var database:SQLiteDatabase? = null

    private var _transaction: STMSQLiteDatabaseTransaction? = null

    override fun run() {

//        STMFunctions.debugLog("Syncer", "operation started")

        if (readOnly){
//            STMFunctions.debugLog("DEBUG", "removing pool database from array")
//            STMFunctions.debugLog("DEBUG", "pool database count before remove: ${adapter.poolDatabases.size}")

            synchronized(adapter.poolDatabases){
                database = adapter.poolDatabases.removeAt(0)
            }

        }else{

            database = adapter.database

        }

        _transaction = STMSQLiteDatabaseTransaction(database!!, adapter)

        _transaction?.operation = this

        synchronized(lock1){
            lock1.notify()
        }

        synchronized(lock2){

            lock2.wait()

        }

    }

    fun finish(){

        if (readOnly){

//            STMFunctions.debugLog("SQLite", "returning pool database to array")

            synchronized(adapter.poolDatabases){
                adapter.poolDatabases.add(database!!)
            }

//            STMFunctions.debugLog("SQLite", "Returned pooldatabase, pools size: ${adapter.poolDatabases.size}")


        }

//        STMFunctions.debugLog("Syncer", "operation ended")

        synchronized(lock2){

            lock2.notify()

        }

//        STMFunctions.debugLog("SQLite", "Finished operation")

    }

}