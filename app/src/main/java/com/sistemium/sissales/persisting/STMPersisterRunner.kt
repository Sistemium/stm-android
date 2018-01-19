package com.sistemium.sissales.persisting

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

        val result = block(readOnlyTransactionCoordinator)

        readOnlyTransactionCoordinator.endTransactionWithSuccess(true)

        return result

    }

    override fun execute(block: (persistingTransaction: STMPersistingTransaction) -> Boolean) {

        val transactionCoordinator = STMPersisterTransactionCoordinator(adapters, false)

        val result = block(transactionCoordinator)

        transactionCoordinator.endTransactionWithSuccess(result)

    }

}