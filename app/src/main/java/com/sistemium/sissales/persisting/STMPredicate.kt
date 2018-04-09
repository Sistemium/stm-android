package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMAdapting

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */

class STMPredicate {

    companion object {

        @JvmStatic
        fun primaryKeyPredicate(values:Array<*>):STMPredicate {

            return if (values.size == 1) {

                STMPredicate("=", STMPredicate(STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY), STMPredicate("\"${values.first().toString()}\""))

            }else {

                val valueArray = values.map { value -> STMPredicate("\"${value.toString()}\"") }

                STMPredicate(" IN ", STMPredicate(STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY), STMPredicate(", " ,valueArray))

            }

        }

        @JvmStatic
        fun comparatorPredicate(map: Map<*,*>):STMPredicate{

            val key = map.keys.first() as String

            var value = map[key] as Map<*,*>

            if (value[value.keys.first()] == null){

                value = hashMapOf("IS" to "NULL")

            }

            var comparator = value.keys.first()

            if (comparator == "==") comparator = "="

            var rightPredicateString = "\"${value[value.keys.first()]}\""

            if (value[value.keys.first()] is Boolean || value[value.keys.first()] is Number || value[value.keys.first()] == "NULL") rightPredicateString = "${value[value.keys.first()]}"

            if (value[value.keys.first()] is ArrayList<*>) {

                val valueArray = (value[value.keys.first()] as ArrayList<*>).map { v -> STMPredicate("\"$v\"") }

                return STMPredicate("IN", STMPredicate(key), STMPredicate(", ", valueArray))

            }

            return STMPredicate(comparator.toString(), STMPredicate(key), STMPredicate(rightPredicateString))

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

            val subPredicates = arrayListOf<STMPredicate>()

            for ((key,value) in filterMap){

                if (key.startsWith("ANY")) {

                    val anyValue = key.split(" ").last()

                    val whereVal = STMPredicate(comparatorPredicate(value).predicateForAdapter(null, null)!!)

                    val predicate = STMPredicate("", arrayListOf(STMPredicate("exists ( select * from "),
                            STMPredicate("?", anyValue),
                            STMPredicate(" where "),
                            whereVal,
                            STMPredicate(" and "),
                            if (anyValue.endsWith("s")) STMPredicate("?", "uncapitalizedTableName") else  STMPredicate(""),
                            STMPredicate("Id = "),
                            if (anyValue.endsWith("s")) STMPredicate("?", "capitalizedTableName") else STMPredicate(anyValue + STMConstants.RELATIONSHIP_SUFFIX),
                            if (anyValue.endsWith("s")) STMPredicate(".id )") else  STMPredicate(" )")
                    ))

                    subPredicates.add(predicate)

                }  else {

                    if (key.endsWith(STMConstants.RELATIONSHIP_SUFFIX) && value.values.first() != null){

                        val xid = value.values.first() as String?

                        val predicate = STMPredicate("nonNull", arrayListOf(STMPredicate("exists ( select * from "),
                                STMPredicate("?", key),
                                if (xid != null) STMPredicate(" where [id] = '$xid' and id = ") else STMPredicate(" where [id] IS NULL and id = "),
                                STMPredicate("relation", key),
                                STMPredicate(")")
                        ))

                        subPredicates.add(predicate)

                    }else{

                        val predicate = comparatorPredicate(hashMapOf(key to value))

                        subPredicates.add(predicate)

                    }

                }

            }

            return combinePredicates(subPredicates)

        }

        @JvmStatic
        fun predicateWithOptions(options:Map<*,*>?, predicate: STMPredicate?):STMPredicate?{

            val isFantom = options?.get(STMConstants.STMPersistingOptionFantoms) as? Boolean ?: false

            val phantomPredicate = STMPredicate("=", STMPredicate("isFantom"), STMPredicate("${if (isFantom) 1 else 0}"))

            if (predicate == null) return phantomPredicate

            return STMPredicate.combinePredicates(arrayListOf(predicate, phantomPredicate))


        }

        @JvmStatic
        fun combinePredicates(subPredicates:ArrayList<STMPredicate>):STMPredicate{

            if (subPredicates.size == 1){

                return subPredicates.first()

            }

            return STMPredicate(") AND (", subPredicates)

        }

    }

    private enum class PredicateType{
        Value, PredicateArray, Comparison
    }

    private var value:String? = null
    private var predicateArray:List<STMPredicate>? = null
    private var leftPredicate:STMPredicate? = null
    private var rightPredicate:STMPredicate? = null
    private var relation:String? = null
    private var type:PredicateType

    constructor(relation: String, value:String){
        this.relation = relation
        this.value = value
        this.type = PredicateType.Value
    }

    constructor(value:String){
        this.value = value
        this.type = PredicateType.Value
    }

    constructor(relation:String, predicateArray:List<STMPredicate>){
        this.relation = relation
        this.predicateArray = predicateArray
        this.type = PredicateType.PredicateArray
    }

    constructor(relation:String, leftPredicate:STMPredicate, rightPredicate:STMPredicate){
        this.relation = relation
        this.leftPredicate = leftPredicate
        this.rightPredicate = rightPredicate
        this.type = PredicateType.Comparison
    }

    override fun toString(): String = throw Exception("should not, use predicateForAdapter instead")

    fun predicateForAdapter(adapter:STMAdapting?, entityName:String?):String?{

        when(type){

            STMPredicate.PredicateType.Value -> {

                if (relation == "?" && adapter != null && entityName != null){

                    if (value == "uncapitalizedTableName") return STMFunctions.removePrefixFromEntityName(entityName).decapitalize()

                    if (value == "capitalizedTableName") return STMFunctions.removePrefixFromEntityName(entityName).capitalize()

                    val relationships = adapter.model.entitiesByName[STMFunctions.addPrefixToEntityName(entityName)]?.relationshipsByName

                    return if (relationships?.containsKey(value) == true) {

                        STMFunctions.removePrefixFromEntityName(relationships[value]?.destinationEntityName!!)

                    } else {

                        value?.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX)?.capitalize()

                    }

                }

                if (relation == "relation"  && adapter != null && entityName != null) {

                    val relationships = adapter.model.entitiesByName[STMFunctions.addPrefixToEntityName(entityName)]?.relationshipsByName

                    return if (relationships?.containsKey(value?.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX)) == true) {

                        value

                    } else {

                        null

                    }


                }

                if (value == "false"){

                    return "0"

                }

                if (value == "true"){

                    return "1"

                }


                return value

            }

            STMPredicate.PredicateType.PredicateArray -> {

                val array = predicateArray!!.mapNotNull {

                    it.predicateForAdapter(adapter, entityName)

                }

                if (relation == "nonNull"){

                    return if (predicateArray!!.size == array.size){

                        "(${array.joinToString("")})"

                    }else{

                        null

                    }

                }

                return "(${array.joinToString(relation!!)})"

            }

            STMPredicate.PredicateType.Comparison -> {

                val left = leftPredicate?.predicateForAdapter(adapter, entityName)

                val right = rightPredicate?.predicateForAdapter(adapter, entityName)

                val valid = entityName == null || adapter == null || adapter.model.fieldsForEntityName(entityName).containsKey(left) || adapter.model.objectRelationshipsForEntityName(entityName).containsKey(left?.removeSuffix(STMConstants.RELATIONSHIP_SUFFIX))

                if (left != null && right != null && valid){

                    return "$left $relation $right"

                }

                return null

            }
        }

    }

}