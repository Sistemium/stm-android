package com.sistemium.sissales.model

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

class STMEntityDescription(entity: Map<*,*>){

    val entityName:String = entity["name"] as? String ?: throw Exception("Wrong entity name")

    val abstract = entity["isAbstract"] == "YES"

    private val attributesByName:MutableMap<String, STMAttributeDescription> = hashMapOf()

    val userInfo:Map<*, *>
        get() = attributesByName["userInfo"] as? Map<*, *> ?: hashMapOf<Any, Any>()

    init {

        val array =  entity["attribute"] as? Array<*>

        array?.filterIsInstance<Map<*, *>>()?.map { STMAttributeDescription(it) }?.forEach { attributesByName[it.attributeName] = it }

    }

}