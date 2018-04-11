package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 10/01/2018.
 */
interface STMPersistingMergeInterceptor {

    fun interceptedAttributes(attributes: Map<*, *>, options: Map<*, *>?, persistingTransaction: STMPersistingTransaction?): Map<*, *>?

}