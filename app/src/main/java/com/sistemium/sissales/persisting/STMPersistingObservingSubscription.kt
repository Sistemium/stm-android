package com.sistemium.sissales.persisting

import java.util.*

/**
 * Created by edgarjanvuicik on 11/01/2018.
 */
class STMPersistingObservingSubscription(var entityName:String? = null, var options:Map<*,*>? = null, var predicate: ((Any) -> Boolean)? = null) {

    var callback: ((ArrayList<*>) -> (Unit))? = null
    var callbackWithEntityName: ((String, ArrayList<*>) -> (Unit))? = null
    val identifier = UUID.randomUUID()!!

}