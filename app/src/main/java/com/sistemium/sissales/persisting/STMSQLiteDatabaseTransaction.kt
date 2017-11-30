package com.sistemium.sissales.persisting

import android.database.sqlite.SQLiteDatabase
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter

/**
 * Created by edgarjanvuicik on 30/11/2017.
 */
class STMSQLiteDatabaseTransaction(database: SQLiteDatabase, adapter:STMSQLiteDatabaseAdapter):STMPersistingTransaction {

    var operation:STMSQLiteDatabaseOperation? = null

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Array<Map<*, *>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}