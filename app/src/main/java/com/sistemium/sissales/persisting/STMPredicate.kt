package com.sistemium.sissales.persisting

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */
class STMPredicate(predicate:String) {

    companion object {
        @JvmStatic
        fun primaryKeyPredicateEntityName(entityName:String, values:Array<*>):STMPredicate {

            return STMPredicate("")

        }
    }

}