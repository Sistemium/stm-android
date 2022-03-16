package com.sistemium.sissales.persisting

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter

/**
 * Created by edgarjanvuicik on 24/11/2017.
 */

class STMSQLiteDatabaseOperation(val readOnly: Boolean, private var adapter: STMSQLiteDatabaseAdapter) : Runnable {

    var success = false

    val transaction: STMSQLiteDatabaseTransaction by lazy {

        synchronized(lock1) {

            while (_transaction == null) {

                lock1.wait()

            }

            return@lazy _transaction!!

        }

    }

    private val lock1 = Object()

    private val lock2 = Object()

    private var database: SQLiteDatabase? = null

    private var _transaction: STMSQLiteDatabaseTransaction? = null

    override fun run() {

        if (readOnly) {

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

            lock2.wait()

        }

    }

    fun finish() {

        synchronized(lock2) {

            if (readOnly) {

                synchronized(adapter.poolDatabases) {
                    adapter.poolDatabases.add(database!!)
                }

            }

            lock2.notify()

        }

    }

}