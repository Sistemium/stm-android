package com.sistemium.sissales.persisting

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter

/**
 * Created by edgarjanvuicik on 24/11/2017.
 */

class STMSQLiteDatabaseOperation(private val readOnly:Boolean, private var adapter: STMSQLiteDatabaseAdapter) :Runnable {

    private val lock1 = Object()

    private val lock2 = Object()

    private var database:SQLiteDatabase? = null

    private var _transaction: STMSQLiteDatabaseTransaction? = null

    val transaction:STMSQLiteDatabaseTransaction by lazy {

        if (_transaction != null) return@lazy _transaction!!

        synchronized(lock1){

            lock1.wait()

            return@lazy _transaction!!

        }

    }

    override fun run() {

        synchronized(lock2){

            if (readOnly){

                database = adapter.poolDatabases.removeAt(0)

            }else{

                database = adapter.database

                database!!.beginTransaction()

            }

            _transaction = STMSQLiteDatabaseTransaction(database!!, adapter)

            _transaction?.operation = this

            synchronized(lock1){
                lock1.notify()
            }

            lock2.wait()

        }

    }

    fun finish(){

        TODO("not implemented")

        if (!readOnly){

//            if (self.success){
//
//                [self.database commit]
//
//            }else{
//
//                [self.database rollback]
//
//            }

        }else{

//            [STMFunctions pushArray:self.stmFMDB.poolDatabases object:self.database];

        }

        lock2.notify() //sync nizabud

    }

}