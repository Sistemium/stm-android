package com.sistemium.sissales.model

import com.sistemium.sissales.base.STMFunctions
import java.io.File
import java.util.*

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

class STMManagedObjectModel(var model: String) {

    var entitiesByName: MutableMap<String, STMEntityDescription> = hashMapOf()

    var userDefinedModelVersionIdentifier: String

    private val gson = STMFunctions.gson

    init {

        val mapModel = gson.fromJson(model, Map::class.java)

        userDefinedModelVersionIdentifier = (mapModel["model"] as? Map<*, *>)?.get("userDefinedModelVersionIdentifier") as? String ?: "367"

        val entityArray = (mapModel["model"] as? Map<*, *>)?.get("entity") as? ArrayList<*>

        entityArray?.forEach {

            val entityDesc = STMEntityDescription(it as Map<*, *>)

            entitiesByName[entityDesc.entityName] = entityDesc

        }

    }

    override fun equals(other: Any?): Boolean {

        if (other !is STMManagedObjectModel) {
            return false
        }

        return entitiesByName == other.entitiesByName
    }

    override fun hashCode(): Int {
        return entitiesByName.hashCode()
    }

    fun saveToFile(path: String) {

        val file = File(path)

        file.parentFile.mkdirs()

        file.createNewFile()

        file.writeText(model)

    }

}