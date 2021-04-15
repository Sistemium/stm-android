package com.sistemium.sissales.persisting

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter

/**
 * Created by edgarjanvuicik on 24/11/2017.
 */

class STMSQLiteDatabaseOperation(val readOnly: Boolean, private var adapter: STMSQLiteDatabaseAdapter) : Runnable {

    var success = false

    val transaction: STMSQLiteDatabaseTransaction by lazy {

        synchronized(lock1) {

            while (_transaction == null) {

                STMFunctions.debugLog("STMSQLiteDatabaseOperation", "sadly I cannot give you transaction read only: $readOnly")

                lock1.wait()

            }

            STMFunctions.debugLog("STMSQLiteDatabaseOperation", "here I can give you a transaction read only: $readOnly")

            return@lazy _transaction!!

        }

    }

    private val lock1 = Object()

    private val lock2 = Object()

    private var database: SQLiteDatabase? = null

    private var _transaction: STMSQLiteDatabaseTransaction? = null

    override fun run() {

        STMFunctions.debugLog("Syncer", "operation started read only: $readOnly")

        if (readOnly) {
            STMFunctions.debugLog("DEBUG", "removing pool database from array read only: $readOnly")
            STMFunctions.debugLog("DEBUG", "pool database count before remove: ${adapter.poolDatabases.size} read only: $readOnly")

            synchronized(adapter.poolDatabases) {
                database = adapter.poolDatabases.removeAt(0)
            }

        } else {

            database = adapter.database

        }

        synchronized(lock2) {

            _transaction = STMSQLiteDatabaseTransaction(database!!, adapter)

            _transaction!!.operation = this

            synchronized(lock1) {
                lock1.notify()
            }



            STMFunctions.debugLog("STMSQLiteDatabaseOperation", "lets now wait for finish read only: $readOnly")

            lock2.wait()

            STMFunctions.debugLog("STMSQLiteDatabaseOperation", "Yay I received finish read only: $readOnly")

        }

    }

    fun finish() {

        synchronized(lock2) {

            if (readOnly) {

                STMFunctions.debugLog("SQLite", "returning pool database to array read only: $readOnly")

                synchronized(adapter.poolDatabases) {
                    adapter.poolDatabases.add(database!!)
                }

                STMFunctions.debugLog("SQLite", "Returned pooldatabase, pools size: ${adapter.poolDatabases.size} read only: $readOnly")


            }

            STMFunctions.debugLog("Syncer", "operation ended read only: $readOnly")

            lock2.notify()

        }

        STMFunctions.debugLog("SQLite", "Finished operation read only: $readOnly")

    }

}