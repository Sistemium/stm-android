package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMAdapting

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */

class STMPredicate {

    enum class PredicateType{
        Value, PredicateArray, Comparison
    }

    constructor(relation: String, value:String){
        this.relation = relation
        this.value = value
        this.type = PredicateType.Value
    }

    constructor(value:String){
        this.relation = relation
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

    private var value:String? = null
    private var predicateArray:List<STMPredicate>? = null
    private var leftPredicate:STMPredicate? = null
    private var rightPredicate:STMPredicate? = null
    private var relation:String? = null
    private var type:PredicateType

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

            val value = map[key] as Map<*,*>

            var comparator = value.keys.first()

            if (comparator == "==") comparator = "="

            var rightPredicateString = "\"${value[value.keys.first()]}\""

            if (value[value.keys.first()] is Boolean || value[value.keys.first()] is Number) rightPredicateString = "${value[value.keys.first()]}"

            val predicate = STMPredicate(comparator.toString(), STMPredicate(key), STMPredicate(rightPredicateString))

            return predicate

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
                            STMPredicate("?", "uncapitalizedTableName"),
                            STMPredicate("Id = "),
                            STMPredicate("?", "capitalizedTableName"),
                            STMPredicate(".id )")
                    ))

                    subPredicates.add(predicate)

                }  else {

                    if (key.endsWith(STMConstants.RELATIONSHIP_SUFFIX)){

                        val xid = value.values.first() as String

                        val predicate = STMPredicate("nonNull", arrayListOf(STMPredicate("exists ( select * from "),
                                STMPredicate("?", key),
                                STMPredicate(" where [id] = '$xid' and id = "),
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
        private fun combinePredicates(subPredicates:ArrayList<STMPredicate>):STMPredicate{

            if (subPredicates.size == 1){

                return subPredicates.first()

            }

            return STMPredicate(") AND (", subPredicates)

        }

    }

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

                val valid = entityName == null || adapter == null || adapter.model.fieldsForEntityName(entityName).containsKey(left)

                if (left != null && right != null && valid){

                    return "$left $relation $right"

                }

                return null

            }
        }

    }

    override fun toString(): String = throw Exception("should not, use predicateForAdapter instead")

}