package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.interfaces.STMPersistingTransaction

/**
 * Created by edgarjanvuicik on 22/11/2017.
 */
class STMPersisterTransactionCoordinator(private val adapters: HashMap<STMStorageType, STMAdapting>, private val readOnly: Boolean) : STMPersistingTransaction {

    override var modellingDelegate: STMModelling? = null

    private val transactions = hashMapOf<STMStorageType, STMPersistingTransaction>()

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): ArrayList<Map<*, *>> {

        val predicateWithOptions = STMPredicate.predicateWithOptions(options, predicate)

        val transaction = transactionForEntityName(entityName, options) ?: throw Exception("wrong entity name: $entityName")

        return transaction.findAllSync(entityName, predicateWithOptions, options)

    }

    override fun mergeWithoutSave(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Map<*, *>? {

        val transaction = transactionForEntityName(entityName, options)

        return transaction?.mergeWithoutSave(entityName, attributes, options)

    }

    override fun destroyWithoutSave(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Int {

        var objects = arrayListOf<Map<*, *>>()

        if (options?.get(STMConstants.STMPersistingOptionRecordstatuses) == null || options[STMConstants.STMPersistingOptionRecordstatuses] == true) {

            objects = findAllSync(entityName, predicate, options)

        }

        val transaction = transactionForEntityName(entityName, options)

        val count = transaction?.destroyWithoutSave(entityName, predicate, options)

        val recordStatuses: ArrayList<Any>? = arrayListOf()

        val recordStatusEntity = STMFunctions.addPrefixToEntityName("RecordStatus")

        for (obj in objects) {

            var recordStatus: Map<*, *>? = hashMapOf(

                    "objectXid" to obj[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY],
                    "name" to STMFunctions.removePrefixFromEntityName(entityName),
                    "isRemoved" to 1

            )

            recordStatus = mergeWithoutSave(recordStatusEntity, recordStatus!!, hashMapOf(STMConstants.STMPersistingOptionRecordstatuses to 0))

            if (recordStatus != null) {

                recordStatuses?.add(recordStatus)

            }

        }

        if (recordStatuses?.count() != null && recordStatuses.count() > 0) {

            transaction?.modellingDelegate?.persistanceDelegate?.notifyObservingEntityName(recordStatusEntity, recordStatuses, options)

        }


        return count ?: 0

    }

    fun endTransactionWithSuccess(success: Boolean) {

        for (key in transactions.keys) {
            val transaction = transactions[key]
            adapters[key]?.endTransactionWithSuccess(transaction!!, success)
        }

        transactions.clear()

    }

    private fun transactionForEntityName(entityName: String, options: Map<*, *>?): STMPersistingTransaction? {

        val storageType = storageForEntityName(entityName, options)

        var transaction = transactions[storageType]

        if (transaction == null && adapters.keys.contains(storageType)) {
            transactions[storageType] = adapters[storageType]!!.beginTransaction(readOnly)
            transaction = transactions[storageType]
        }

        return transaction

    }

    private fun storageForEntityName(entityName: String, options: Map<*, *>?): STMStorageType {

        if (options?.get(STMConstants.STMPersistingOptionForceStorage) as? STMStorageType != null) {

            return options[STMConstants.STMPersistingOptionForceStorage] as STMStorageType

        }

        for (adapter in adapters.values) {

            val storage = adapter.model.storageForEntityName(entityName)

            if (storage != STMStorageType.STMStorageTypeNone) {
                return adapter.model.storageForEntityName(entityName)
            }
        }

        return STMStorageType.STMStorageTypeNone

    }

}