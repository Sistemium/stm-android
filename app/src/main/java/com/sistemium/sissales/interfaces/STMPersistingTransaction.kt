package com.sistemium.sissales.interfaces

import com.sistemium.sissales.persisting.STMPredicate

/**
 * Created by edgarjanvuicik on 21/11/2017.
 */
interface STMPersistingTransaction {

    var modellingDelegate: STMModelling?

    fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): ArrayList<Map<*, *>>

    fun mergeWithoutSave(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Map<*, *>?

    fun destroyWithoutSave(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Int

}