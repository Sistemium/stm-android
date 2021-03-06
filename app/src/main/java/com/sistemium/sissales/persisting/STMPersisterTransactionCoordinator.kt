package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.interfaces.STMPersistingTransaction

/**
 * Created by edgarjanvuicik on 22/11/2017.
 */
class STMPersisterTransactionCoordinator(private val adapters: HashMap<STMStorageType, STMAdapting>, private val readOnly: Boolean) : STMPersistingTransaction {

    private val transactions = hashMapOf<STMStorageType, STMPersistingTransaction>()

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): ArrayList<Map<*, *>> {

        val predicateWithOptions = STMPredicate.predicateWithOptions(options, predicate)

        val transaction = transactionForEntityName(entityName, options)

        return transaction.findAllSync(entityName, predicateWithOptions, options)

    }

    override fun mergeWithoutSave(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Map<*, *>? {

        val transaction = transactionForEntityName(entityName, options)

        return transaction.mergeWithoutSave(entityName, attributes, options)

    }

    override fun destroyWithoutSave(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Int {

        val transaction = transactionForEntityName(entityName, options)

        return transaction.destroyWithoutSave(entityName, predicate, options)

    }

    fun endTransactionWithSuccess(success: Boolean) {

        for (key in transactions.keys) {
            val transaction = transactions[key]
            adapters[key]?.endTransactionWithSuccess(transaction!!, success)
        }

        transactions.clear()

    }

    private fun transactionForEntityName(entityName: String, options: Map<*, *>?): STMPersistingTransaction {

        val storageType = storageForEntityName(entityName, options)

        var transaction = transactions[storageType]

        if (transaction == null && adapters.keys.contains(storageType)) {
            transactions[storageType] = adapters[storageType]!!.beginTransaction(readOnly)
            transaction = transactions[storageType]
        }

        if (transaction == null && !adapters.keys.contains(storageType)){

            var keys = ""

            for (key in adapters.keys){

                keys += "$key ,"
            }

            throw Exception("Wrong entity name: $entityName, storage type : $storageType, has adapters for storage types $keys")

        }

        return transaction!!

    }

    private fun storageForEntityName(entityName: String, options: Map<*, *>?): STMStorageType {

        if (options?.get(STMConstants.STMPersistingOptionForceStorage) as? STMStorageType != null) {

            return options[STMConstants.STMPersistingOptionForceStorage] as STMStorageType

        }

        return STMModelling.sharedModeler!!.storageForEntityName(entityName)

    }

}