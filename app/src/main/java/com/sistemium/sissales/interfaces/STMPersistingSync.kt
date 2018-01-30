package com.sistemium.sissales.interfaces

import com.sistemium.sissales.persisting.STMPredicate

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */
interface STMPersistingSync {

    @Throws(Exception::class)
    fun findSync(entityName:String, identifier:String, options:Map<*,*>?):Map<*,*>

    @Throws(Exception::class)
    fun findAllSync(entityName:String, predicate:STMPredicate?, options:Map<*,*>?):ArrayList<Map<*,*>>

    @Throws(Exception::class)
    fun mergeSync(entityName:String, attributes:Map<*,*>, options:Map<*,*>?):Map<*,*>

    @Throws(Exception::class)
    fun mergeManySync(entityName:String, attributeArray:ArrayList<*>, options:Map<*,*>?):ArrayList<Map<*,*>>

    @Throws(Exception::class)
    fun destroySync(entityName: String, identifier: String, options: Map<*, *>?):Boolean

    @Throws(Exception::class)
    fun destroyAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?):Int

}