package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 25/01/2018.
 */
interface STMPersistingObservable {

    fun notifyObservingEntityName(entityName: String, items: ArrayList<*>, options: Map<*, *>?)
    fun observeEntity(entityName: String, predicate: ((Any) -> Boolean)?, options: Map<*, *>, callback: (ArrayList<*>) -> (Unit)): String
    fun cancelSubscription(subscriptionId: String): Boolean

}