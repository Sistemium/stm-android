package com.sistemium.sissales.model

import com.google.gson.Gson
import java.io.File
import java.util.*

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

class STMManagedObjectModel(var model: String){

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

    fun saveToFile(path:String){

        val file = File(path)

        file.createNewFile()

        file.writeText(model)

    }

    override fun equals(other: Any?): Boolean {

        if (other !is STMManagedObjectModel){
            return false
        }

        return entitiesByName == other.entitiesByName
    }

    override fun hashCode(): Int {
        return entitiesByName.hashCode()
    }

}