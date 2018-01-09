package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */
interface STMPersistingRunning {

    fun readOnly(block: (persistingTransaction: STMPersistingTransaction) -> ArrayList<Map<*,*>>): ArrayList<Map<*, *>>

}