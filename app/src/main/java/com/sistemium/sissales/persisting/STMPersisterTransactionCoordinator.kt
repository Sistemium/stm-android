package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMPersistingTransaction

/**
 * Created by edgarjanvuicik on 22/11/2017.
 */
class STMPersisterTransactionCoordinator(private val adapters:HashMap<STMStorageType, STMAdapting>, private val readOnly:Boolean):STMPersistingTransaction {

    private val transactions = hashMapOf<STMStorageType,STMPersistingTransaction>()

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Array<Map<*, *>> {

        val predicateWithOptions = STMPredicate.predicateWithOptions(options, predicate)

        val transaction = transactionForEntityName(entityName, options)

        return transaction.findAllSync(entityName, predicateWithOptions, options)

    }

    fun endTransactionWithSuccess(success:Boolean){

        TODO("not implemented")

    }

    private fun transactionForEntityName(entityName:String, options:Map<*,*>?):STMPersistingTransaction{

        val storageType = storageForEntityName(entityName, options)

        var transaction = transactions[storageType]

        if (transaction == null && adapters.keys.contains(storageType)){
            transactions[storageType] = adapters[storageType]!!.beginTransactionReadOnly()
            transaction = transactions[storageType]
        }

        return transaction ?: throw Exception("wrong entity name: $entityName")

    }

    private fun storageForEntityName(entityName: String, options: Map<*, *>?):STMStorageType{

        if (options?.get(STMConstants.STMPersistingOptionForceStorage) as? STMStorageType != null){

            return options!![STMConstants.STMPersistingOptionForceStorage] as STMStorageType

        }

        for (adapter in adapters.values){

            val storage = adapter.model.storageForEntityName(entityName)

            if (storage != STMStorageType.STMStorageTypeNone){
                return adapter.model.storageForEntityName(entityName)
            }
        }

        return STMStorageType.STMStorageTypeNone

    }

}