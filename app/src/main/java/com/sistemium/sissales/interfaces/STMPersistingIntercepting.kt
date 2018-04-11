package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 22/01/2018.
 */
interface STMPersistingIntercepting {

    fun beforeMergeEntityName(entityName: String, interceptor: STMPersistingMergeInterceptor?)

    fun applyMergeInterceptors(entityName: String, attributes: Map<*, *>, options: Map<*, *>?, persistingTransaction: STMPersistingTransaction?): Map<*, *>?

}