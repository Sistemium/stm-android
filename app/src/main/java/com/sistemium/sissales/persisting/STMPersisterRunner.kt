package com.sistemium.sissales.persisting

import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMPersistingRunning
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import com.sistemium.sissales.model.SQLiteDatabaseAdapter

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */

class STMPersisterRunner(adapters:HashMap<STMStorageType, SQLiteDatabaseAdapter>):STMPersistingRunning {

    override fun readOnly(block: (persistingTransaction: STMPersistingTransaction) -> Array<Map<*, *>>):Array<Map<*, *>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}