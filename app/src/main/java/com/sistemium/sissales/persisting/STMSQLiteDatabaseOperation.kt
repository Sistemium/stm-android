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

    override fun run() {

        TODO("not implemented")

        synchronized(lock1){

            if (readOnly){

//                database = adapter.poolDatabases

            }else{

    //            self.database = self.stmFMDB.database;

    //            [self.database beginTransaction];

            }

    //        self.transaction = [[STMFmdbTransaction alloc] initWithFMDatabase:self.database stmFMDB:self.stmFMDB]

    //        self.transaction.operation = self

            lock1.notify()

            lock2.wait()

        }

    }

    fun waitUntilTransactionIsReady(){

        synchronized(lock1){

            lock1.wait()

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

        lock2.notify()

    }

}