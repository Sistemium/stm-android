package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMPersistingRunning
import com.sistemium.sissales.interfaces.STMPersistingTransaction

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */

class STMPersisterRunner(private val adapters:HashMap<STMStorageType, STMAdapting>):STMPersistingRunning {

    override fun readOnly(block: (persistingTransaction: STMPersistingTransaction) -> ArrayList<Map<*, *>>):ArrayList<Map<*, *>> {

        val readOnlyTransactionCoordinator = STMPersisterTransactionCoordinator(adapters, true)

        try {

            val result = block(readOnlyTransactionCoordinator)

            readOnlyTransactionCoordinator.endTransactionWithSuccess(true)

            //        STMFunctions.debugLog("RUNNER", "Read only transaction ended")

            return result

        }catch (e:Exception){

            readOnlyTransactionCoordinator.endTransactionWithSuccess(false)

            STMFunctions.debugLog("STMPersisterRunner", "Error: ${e.localizedMessage}")

            throw Exception(e)

        }

    }

    override fun execute(block: (persistingTransaction: STMPersistingTransaction) -> Boolean) {

        val transactionCoordinator = STMPersisterTransactionCoordinator(adapters, false)

        try {

            val result = block(transactionCoordinator)

            transactionCoordinator.endTransactionWithSuccess(result)

        } catch (e:Exception){

            transactionCoordinator.endTransactionWithSuccess(false)

            STMFunctions.debugLog("STMPersisterRunner", "Error: ${e.localizedMessage}")

            throw Exception(e)

        }

    }

}