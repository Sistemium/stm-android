package com.sistemium.sissales.model

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

class STMEntityDescription(entity: Map<*,*>){

    val entityName:String = entity["name"] as? String ?: throw Exception("Wrong entity name")

    val abstract = entity["isAbstract"] == "YES"

    val attributesByName:MutableMap<String, STMAttributeDescription> = hashMapOf()

    val relationshipsByName:MutableMap<String, STMRelationshipDescription> = hashMapOf()

    val userInfo:Map<*, *>
        get() = attributesByName["userInfo"] as? Map<*, *> ?: hashMapOf<Any, Any>()

    val parentEntity = entity["parentEntity"] as? String

    init {

        var array = entity["attribute"] as? ArrayList<*>

        if (entity["attribute"] is Map<*,*>){

            array = arrayListOf(entity["attribute"] as Map<*,*>)

        }

        array?.filterIsInstance<Map<*, *>>()?.map { STMAttributeDescription(it) }?.forEach { attributesByName[it.attributeName] = it }

        array = entity["relationship"] as? ArrayList<*>

        if (entity["relationship"] is Map<*,*>){

            array = arrayListOf(entity["relationship"] as Map<*,*>)

        }

        array?.filterIsInstance<Map<*, *>>()?.map { STMRelationshipDescription(it) }?.forEach { relationshipsByName[it.relationshipName] = it }

    }

    override fun equals(other: Any?): Boolean {

        if (other !is STMEntityDescription){

            return false

        }

        return entityName == other.entityName && abstract == other.abstract && attributesByName == other.attributesByName && userInfo == other.userInfo

    }

    override fun hashCode(): Int {
        var result = entityName.hashCode()
        result = 31 * result + abstract.hashCode()
        result = 31 * result + attributesByName.hashCode()
        result = 31 * result + userInfo.hashCode()
        return result
    }

}