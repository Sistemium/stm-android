package com.sistemium.sissales.DataModel

import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMModelling
import java.io.BufferedReader
import java.io.File

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */

class STMModeller(modelName:String) : STMModelling{

    override val managedObjectModel: STMManagedObjectModel

    override val concreteEntities: Map<String, STMEntityDescription>

    init {
        managedObjectModel = modelWithName(modelName)
        concreteEntities = hashMapOf()
    }

    override fun storageForEntityName(entityName: String): STMStorageType {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun isConcreteEntityName(entityName: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun entitiesByName(entityName: String): Map<String, STMEntityDescription> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun fieldsForEntityName(entityName: String): Map<String, STMAttributeDescription> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun objectRelationshipsForEntityName(entityName: String, isToMany: Boolean, cascade: Boolean): Map<String, STMRelationshipDescription> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun objectRelationshipsForEntityName(entityName: String, isToMany: Boolean, cascade: Boolean, optional: Boolean): Map<String, STMRelationshipDescription> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun toOneRelationshipsForEntityName(entityName: String): Map<String, String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun objectRelationshipsForEntityName(entityName: String, isToMany: Boolean): Map<String, String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hierarchyForEntityName(entityName: String): Set<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun modelWithName(modelName:String): STMManagedObjectModel{

        val bufferedReader: BufferedReader = File("$modelName.xml").bufferedReader()

        val xmlModelString = bufferedReader.use { it.readText() }

        return STMManagedObjectModel(xmlModelString)

    }

}
