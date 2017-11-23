package com.sistemium.sissales.model

import com.google.gson.Gson

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

class STMManagedObjectModel(model: String){

    private val gson = Gson()

    var entitiesByName:MutableMap<String, STMEntityDescription> = hashMapOf()

    init {

        val mapModel = gson.fromJson(model, Map::class.java)

        val entityArray = (mapModel["model"] as? Map<* , *>)?.get("entity") as? ArrayList<*>

        entityArray?.forEach {

            val entityDesc = STMEntityDescription(it as Map<*, *>)

            entitiesByName[entityDesc.entityName] = entityDesc
        }
    }

}