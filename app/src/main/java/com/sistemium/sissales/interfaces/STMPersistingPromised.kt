package com.sistemium.sissales.interfaces

import com.sistemium.sissales.persisting.STMPredicate
import nl.komponents.kovenant.Promise

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */

interface STMPersistingPromised {

    fun find(entityName: String, identifier: String, options: Map<*, *>?): Promise<Map<*, *>?, Exception>

    fun findAll(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Promise<ArrayList<Map<*, *>>, Exception>

    fun merge(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Promise<Map<*, *>?, Exception>

    fun mergeMany(entityName: String, attributeArray: ArrayList<*>, options: Map<*, *>?): Promise<ArrayList<Map<*, *>>, Exception>

    fun destroy(entityName: String, identifier: String, options: Map<*, *>?): Promise<Boolean, Exception>

}