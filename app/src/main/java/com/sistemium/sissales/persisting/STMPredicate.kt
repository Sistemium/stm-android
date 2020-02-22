package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMModelling
import java.util.function.Predicate

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */

class STMPredicate(var value: String) {

    companion object {

        @JvmStatic
        fun primaryKeyPredicate(values: Array<*>): STMPredicate {

            return if (values.size == 1) {

                STMPredicate("${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY} = '${values.first().toString()}'")

            } else {

                val valueArray = values.map { value -> STMPredicate("'${value.toString()}'") }

                STMPredicate("${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY} IN ('${valueArray.joinToString("', '")}')")

            }

        }

        @JvmStatic
        fun comparatorPredicate(map: Map<*, *>, entityName: String): STMPredicate? {

            val key = map.keys.first() as String

            var value = map[key] as Map<*, *>

            if (value[value.keys.first()] == null && value.keys.first() == "==") {

                value = hashMapOf("IS" to "NULL")

            }

            if (value[value.keys.first()] == null && value.keys.first() == "!=") {

                value = hashMapOf("IS NOT" to "NULL")

            }

            var comparator = value.keys.first()

            if (comparator == "==") comparator = "="

            if (comparator == "!=") comparator = "<>"

            if (comparator == "likei") comparator = "like"

            var rightPredicateString = "'${value[value.keys.first()]}'"

            if (value[value.keys.first()] is Boolean || value[value.keys.first()] is Number || value[value.keys.first()] == "NULL") rightPredicateString = "${value[value.keys.first()]}"

            if (value[value.keys.first()] is ArrayList<*>) {

                val valueArray = (value[value.keys.first()] as ArrayList<*>).map { v -> STMPredicate("'$v'") }

                return STMPredicate("$key IN (${valueArray.joinToString(", ")})")

            }

            if (
                    STMModelling.sharedModeler!!
                            .fieldsForEntityName(entityName)
                            .containsKey(key) ||
                    STMModelling.sharedModeler!!
                            .toOneRelationshipsForEntityName(entityName)
                            .containsKey(key.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX))
            ){

                return STMPredicate("$key $comparator $rightPredicateString")

            }

            return null

        }

        @JvmStatic
        fun filterPredicate(filter: Map<*, *>?, where: Map<*, *>?, entityName: String): STMPredicate? {

            val filterMap: MutableMap<String, Map<*, *>> = hashMapOf()

            if (where != null) {
                for ((key, value) in where) {
                    filterMap[key as String] = value as Map<*, *>
                }
            }

            if (filter != null) {
                for (key in filter.keys) {
                    filterMap[key as String] = hashMapOf("==" to filter[key])
                }
            }

            if (filterMap.isEmpty()) return null

            val subPredicates = arrayListOf<STMPredicate>()

            for ((key, value) in filterMap) {

                if (key.startsWith("ANY")) {

                    val anyValue = key.split(" ").last()

                    val relationships = STMModelling.sharedModeler!!.objectRelationshipsForEntityName(STMFunctions.addPrefixToEntityName(entityName))

                    val relTable = if (relationships.containsKey(anyValue)) {

                        STMFunctions.removePrefixFromEntityName(relationships[anyValue]!!.destinationEntityName)

                    } else {

                        anyValue.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX).capitalize()

                    }

                    val whereVal = comparatorPredicate(value, relTable) ?: continue

                    val isToMany = STMModelling.sharedModeler!!.toManyRelationshipsForEntityName(entityName).containsKey(anyValue)

                    val condition = if (isToMany) {
                        STMFunctions.removePrefixFromEntityName(entityName).decapitalize()+
                                "Id = "+
                                STMFunctions.removePrefixFromEntityName(entityName).capitalize()+
                                ".id)"
                    } else {
                        "Id = "+
                                anyValue +
                                STMConstants.RELATIONSHIP_SUFFIX+
                                ")"
                    }

                    val predicate = STMPredicate(
                            "exists ( select * from $relTable where $whereVal and $condition"
                    )

                    subPredicates.add(predicate)

                } else {

                    if (key.endsWith(STMConstants.RELATIONSHIP_SUFFIX) && value.values.first() != null && value.values.first() !is ArrayList<*>) {

                        val xid = value.values.first() as String?

                        val relationships = STMModelling.sharedModeler!!.objectRelationshipsForEntityName(STMFunctions.addPrefixToEntityName(entityName))

                        val relTable = if (relationships.containsKey(key)) {

                            STMFunctions.removePrefixFromEntityName(relationships[key]!!.destinationEntityName)

                        } else {

                            STMFunctions.removePrefixFromEntityName(relationships[key.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX)]?.destinationEntityName ?: "")

                        }

                        val relation = if (relationships.containsKey(key.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX))) {

                            key

                        } else {

                            continue

                        }

                        val predicate = STMPredicate("exists ( select * from $relTable ${if (xid != null) " where [id] = '$xid' and id = " else " where [id] IS NULL and id = "}$relation)")

                        subPredicates.add(predicate)

                    } else {

                        val predicate = comparatorPredicate(hashMapOf(key to value), entityName)

                        if (predicate != null){

                            subPredicates.add(predicate)

                        }

                    }

                }

            }

            return combinePredicates(subPredicates)

        }

        @JvmStatic
        fun predicateWithOptions(options: Map<*, *>?, predicate: STMPredicate?): STMPredicate? {

            val isFantom = options?.get(STMConstants.STMPersistingOptionFantoms) as? Boolean
                    ?: false

            val phantomPredicate = STMPredicate("isFantom = ${if (isFantom) 1 else 0}")

            if (predicate == null) return phantomPredicate

            return STMPredicate.combinePredicates(arrayListOf(predicate, phantomPredicate))


        }

        @JvmStatic
        fun combinePredicates(subPredicates: ArrayList<STMPredicate>): STMPredicate? {

            if (subPredicates.size == 0) {

                return null

            }

            if (subPredicates.size == 1) {

                return subPredicates.first()

            }

            return STMPredicate("(${subPredicates.joinToString(") AND (")})")

        }

    }

    init {
        this.value = this.value.replace("false", "0")
        this.value = this.value.replace("true", "1")
    }

    override fun toString(): String = value

}