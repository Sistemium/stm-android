package com.sistemium.sissales.persisting

import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMPersistingRunning
import com.sistemium.sissales.interfaces.STMPersistingTransaction

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */

class STMPersisterRunner(private val adapters:HashMap<STMStorageType, STMAdapting>):STMPersistingRunning {

    override fun readOnly(block: (persistingTransaction: STMPersistingTransaction) -> Array<Map<*, *>>):Array<Map<*, *>> {

        val readOnlyTransactionCoordinator = STMPersisterTransactionCoordinator(adapters, true)

        val result = block(readOnlyTransactionCoordinator)

        readOnlyTransactionCoordinator.endTransactionWithSuccess(true)

        return result

    }

}