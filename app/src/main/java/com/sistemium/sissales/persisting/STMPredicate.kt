package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.interfaces.STMAdapting
import kotlin.reflect.KClass

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */
class STMPredicate(private val predicates: Map<KClass<STMAdapting>,String>) {

    companion object {
        @JvmStatic
        fun primaryKeyPredicateEntityName(entityName:String, values:Array<*>):STMPredicate {

            if (values.size == 1) return STMPredicate(hashMapOf(
                    STMAdapting::class to "${STMConstants.ANKO_PRIMARY_KEY} = ${values.first().toString()}"
            ))

            return STMPredicate(hashMapOf(STMAdapting::class to "${STMConstants.ANKO_PRIMARY_KEY} = ${values.first().toString()}"))

        }
    }

    fun predicateForAdapter(adapter:STMAdapting):String? = predicates[adapter::class]

}