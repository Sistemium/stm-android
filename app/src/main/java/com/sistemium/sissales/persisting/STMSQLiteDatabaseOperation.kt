package com.sistemium.sissales.persisting

import android.database.sqlite.SQLiteDatabase
import android.util.Log
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter

/**
 * Created by edgarjanvuicik on 24/11/2017.
 */

class STMSQLiteDatabaseOperation(val readOnly:Boolean, private var adapter: STMSQLiteDatabaseAdapter) :Runnable {

    private val lock1 = Object()

    private val lock2 = Object()

    private var database:SQLiteDatabase? = null

    private var _transaction: STMSQLiteDatabaseTransaction? = null

    var success = false

    val transaction:STMSQLiteDatabaseTransaction by lazy {

        if (_transaction != null) return@lazy _transaction!!

        synchronized(lock1){

            lock1.wait()

            return@lazy _transaction!!

        }

    }

    override fun run() {

        if (readOnly){

            Log.d("DEBUG", "removing pool database from array")
            Log.d("DEBUG", "pool database count before remove: ${adapter.poolDatabases.size}")

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

            Log.d("Debug", "returning pool database to array")
            Log.d("DEBUG", "pool database count before append: ${adapter.poolDatabases.size}")

            synchronized(adapter.poolDatabases){
                adapter.poolDatabases.add(database!!)
            }

        }

        synchronized(lock2){

            lock2.notify()

        }

    }

}