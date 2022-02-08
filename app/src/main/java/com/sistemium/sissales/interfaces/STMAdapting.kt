package com.sistemium.sissales.interfaces

import com.sistemium.sissales.enums.STMStorageType

/**
 * Created by edgarjanvuicik on 15/11/2017.
 */

interface STMAdapting {

    var storageType: STMStorageType
    var ignoredAttributeNames: Array<String>
    var builtInAttributeNames: Array<String>
    var columnsByTable: Map<String, Map<String, String>>?

    fun beginTransaction(readOnly: Boolean = false): STMPersistingTransaction
    fun endTransactionWithSuccess(transaction: STMPersistingTransaction, success: Boolean = false)

    fun close()

}