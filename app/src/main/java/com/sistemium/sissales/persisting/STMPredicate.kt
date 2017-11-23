package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.interfaces.STMAdapting

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */
class STMPredicate(private val predicate: String) {

    companion object {

        @JvmStatic
        fun primaryKeyPredicateEntityName(values:Array<*>):STMPredicate {

            if (values.size == 1) return STMPredicate("${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY} = \"${values.first().toString()}\"")

            return STMPredicate("${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY} IN (${values.joinToString(", ") { value -> "\"${value.toString()}\"" }})")

        }

        @JvmStatic
        fun filterPredicate(filter:Map<*,*>?, where:Map<*,*>?):STMPredicate? {

            val filterMap:MutableMap<String,Map<*,*>> = hashMapOf()

            if (where != null) {
                for ((key,value) in where){
                    filterMap[key as String] = value as Map<*, *>
                }
            }

            if (filter != null) {
                for (key in filter.keys) {
                    filterMap[key as String] = hashMapOf("==" to filter[key])
                }
            }

            if (filterMap.isEmpty()) return null

            val subPredicates = arrayListOf<String>()

            for ((key,value) in filterMap){

                if (key.startsWith("ANY")) {

                    subPredicates.add("$key.${STMPredicate.filterPredicate(null, value).toString()}")

                } else {

                    var comparator = value.keys.first()

                    if (comparator == "==") comparator = "="

                    subPredicates.add("$key $comparator \"${value[value.keys.first()]}\"")

                }

            }

            return combinePredicates(subPredicates)

        }

        @JvmStatic
        fun predicateWithOptions(options:Map<*,*>?, predicate: STMPredicate?):STMPredicate?{

            if (options?.get(STMConstants.STMPersistingOptionFantoms) as? String != null){

                val phantomPredicate = STMPredicate("isFantom = ${(options!![STMConstants.STMPersistingOptionFantoms] as String).toBoolean()}")

                return STMPredicate.combinePredicates(arrayListOf(predicate, phantomPredicate))

            }

            return predicate

        }

        @JvmStatic

        private fun combinePredicates(subPredicates:ArrayList<*>):STMPredicate =
                STMPredicate("(${subPredicates.joinToString(") AND (")})")

        @JvmStatic
        fun predicateWithOptions(options:Map<*,*>):STMPredicate? = predicateWithOptions(options, null)

    }

    fun predicateForAdapter(adapter:STMAdapting):String?{

        // need to use adapters modeling
        TODO("not implemented")

    }

    override fun toString(): String = predicate

}