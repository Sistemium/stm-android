package com.sistemium.sissales.persisting

import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMPersistingTransaction

/**
 * Created by edgarjanvuicik on 22/11/2017.
 */
class STMPersisterTransactionCoordinator(private val adapters:HashMap<STMStorageType, STMAdapting>, private val readOnly:Boolean):STMPersistingTransaction {

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Array<Map<*, *>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    fun endTransactionWithSuccess(success:Boolean){

        TODO("not implemented")

    }

}