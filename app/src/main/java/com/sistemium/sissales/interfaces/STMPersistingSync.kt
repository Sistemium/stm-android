package com.sistemium.sissales.interfaces

import com.sistemium.sissales.persisting.STMPredicate

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */
interface STMPersistingSync {

    @Throws(Exception::class)
    fun findSync(entityName:String, identifier:String, options:Map<*,*>?):Map<*,*>

    @Throws(Exception::class)
    fun findAllSync(entityName:String, predicate:STMPredicate?, options:Map<*,*>?):Array<Map<*,*>>

}