package com.sistemium.sissales.interfaces

import com.sistemium.sissales.persisting.STMPredicate

/**
 * Created by edgarjanvuicik on 21/11/2017.
 */
interface STMPersistingTransaction {

    fun findAllSync(entityName:String, predicate: STMPredicate?, options:Map<*,*>?):ArrayList<Map<*,*>>

}