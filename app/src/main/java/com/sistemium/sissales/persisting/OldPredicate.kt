package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMAdapting
import com.sistemium.sissales.interfaces.STMModelling

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */

class OldPredicate {

    companion object {

        @JvmStatic
        fun primaryKeyPredicate(values: Array<*>): OldPredicate {

            return if (values.size == 1) {

                OldPredicate("=", OldPredicate(STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY), OldPredicate("'${values.first().toString()}'"))

            } else {

                val valueArray = values.map { value -> OldPredicate("'${value.toString()}'") }

                OldPredicate(" IN ", OldPredicate(STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY), OldPredicate(", ", valueArray))

            }

        }

        @JvmStatic
        fun comparatorPredicate(map: Map<*, *>): OldPredicate {

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

            var rightPredicateString = "'${value[value.keys.first()]}'"

            if (value[value.keys.first()] is Boolean || value[value.keys.first()] is Number || value[value.keys.first()] == "NULL") rightPredicateString = "${value[value.keys.first()]}"

            if (value[value.keys.first()] is ArrayList<*>) {

                val valueArray = (value[value.keys.first()] as ArrayList<*>).map { v -> OldPredicate("'$v'") }

                return OldPredicate("IN", OldPredicate(key), OldPredicate(", ", valueArray))

            }

            return OldPredicate(comparator.toString(), OldPredicate(key), OldPredicate(rightPredicateString))

        }

        @JvmStatic
        fun filterPredicate(filter: Map<*, *>?, where: Map<*, *>?): OldPredicate? {

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

            val subPredicates = arrayListOf<OldPredicate>()

            for ((key, value) in filterMap) {

                if (key.startsWith("ANY")) {

                    val anyValue = key.split(" ").last()

                    val whereVal = OldPredicate(comparatorPredicate(value).predicateForModel(null, null)!!)

                    val predicate = OldPredicate("", arrayListOf(OldPredicate("exists ( select * from "),
                            OldPredicate("?", anyValue),
                            OldPredicate(" where "),
                            whereVal,
                            OldPredicate(" and "),
                            if (anyValue.endsWith("s")) OldPredicate("?", "uncapitalizedTableName") else OldPredicate(""),
                            OldPredicate("Id = "),
                            if (anyValue.endsWith("s")) OldPredicate("?", "capitalizedTableName") else OldPredicate(anyValue + STMConstants.RELATIONSHIP_SUFFIX),
                            if (anyValue.endsWith("s")) OldPredicate(".id )") else OldPredicate(" )")
                    ))

                    subPredicates.add(predicate)

                } else {

                    if (key.endsWith(STMConstants.RELATIONSHIP_SUFFIX) && value.values.first() != null) {

                        val xid = value.values.first() as String?

                        val predicate = OldPredicate("nonNull", arrayListOf(OldPredicate("exists ( select * from "),
                                OldPredicate("?", key),
                                if (xid != null) OldPredicate(" where [id] = '$xid' and id = ") else OldPredicate(" where [id] IS NULL and id = "),
                                OldPredicate("relation", key),
                                OldPredicate(")")
                        ))

                        subPredicates.add(predicate)

                    } else {

                        val predicate = comparatorPredicate(hashMapOf(key to value))

                        subPredicates.add(predicate)

                    }

                }

            }

            return combinePredicates(subPredicates)

        }

        @JvmStatic
        fun predicateWithOptions(options: Map<*, *>?, predicate: OldPredicate?): OldPredicate? {

            val isFantom = options?.get(STMConstants.STMPersistingOptionFantoms) as? Boolean
                    ?: false

            val phantomPredicate = OldPredicate("=", OldPredicate("isFantom"), OldPredicate("${if (isFantom) 1 else 0}"))

            if (predicate == null) return phantomPredicate

            return OldPredicate.combinePredicates(arrayListOf(predicate, phantomPredicate))


        }

        @JvmStatic
        fun combinePredicates(subPredicates: ArrayList<OldPredicate>): OldPredicate {

            if (subPredicates.size == 1) {

                return subPredicates.first()

            }

            return OldPredicate(") AND (", subPredicates)

        }

    }

    private enum class PredicateType {
        Value, PredicateArray, Comparison
    }

    private var value: String? = null
    private var predicateArray: List<OldPredicate>? = null
    private var leftPredicate: OldPredicate? = null
    private var rightPredicate: OldPredicate? = null
    private var relation: String? = null
    private var type: PredicateType

    constructor(relation: String, value: String) {
        this.relation = relation
        this.value = value
        this.type = PredicateType.Value
    }

    constructor(value: String) {
        this.value = value
        this.type = PredicateType.Value
    }

    constructor(relation: String, predicateArray: List<OldPredicate>) {
        this.relation = relation
        this.predicateArray = predicateArray
        this.type = PredicateType.PredicateArray
    }

    constructor(relation: String, leftPredicate: OldPredicate, rightPredicate: OldPredicate) {
        this.relation = relation
        this.leftPredicate = leftPredicate
        this.rightPredicate = rightPredicate
        this.type = PredicateType.Comparison
    }

    override fun toString(): String = throw Exception("should not, use predicateForAdapter instead")

    fun predicateForModel(model: STMModelling?, entityName: String?): String? {

        when (type) {

            OldPredicate.PredicateType.Value -> {

                if (relation == "?" && model != null && entityName != null) {

                    if (value == "uncapitalizedTableName") return STMFunctions.removePrefixFromEntityName(entityName).decapitalize()

                    if (value == "capitalizedTableName") return STMFunctions.removePrefixFromEntityName(entityName).capitalize()

                    val relationships = model.entitiesByName[STMFunctions.addPrefixToEntityName(entityName)]?.relationshipsByName

                    return if (relationships?.containsKey(value) == true) {

                        STMFunctions.removePrefixFromEntityName(relationships[value]?.destinationEntityName!!)

                    } else {

                        value?.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX)?.capitalize()

                    }

                }

                if (relation == "relation" && model != null && entityName != null) {

                    val relationships = model.entitiesByName[STMFunctions.addPrefixToEntityName(entityName)]?.relationshipsByName

                    return if (relationships?.containsKey(value?.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX)) == true) {

                        value

                    } else {

                        null

                    }


                }

                if (value == "false") {

                    return "0"

                }

                if (value == "true") {

                    return "1"

                }


                return value

            }

            OldPredicate.PredicateType.PredicateArray -> {

                val array = predicateArray!!.mapNotNull {

                    it.predicateForModel(model, entityName)

                }

                if (relation == "nonNull") {

                    return if (predicateArray!!.size == array.size) {

                        "(${array.joinToString("")})"

                    } else {

                        null

                    }

                }

                return "(${array.joinToString(relation!!)})"

            }

            OldPredicate.PredicateType.Comparison -> {

                val left = leftPredicate?.predicateForModel(model, entityName)

                val right = rightPredicate?.predicateForModel(model, entityName)

                val valid = entityName == null || model == null || model.fieldsForEntityName(entityName).containsKey(left) || model.objectRelationshipsForEntityName(entityName).containsKey(left?.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX))

                if (left != null && right != null && valid) {

                    return "$left $relation $right"

                }

                return null

            }
        }

    }

}