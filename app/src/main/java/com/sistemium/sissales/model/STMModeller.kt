package com.sistemium.sissales.model

import android.content.Context
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMModelling
import java.util.*

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */

class STMModeller(context: Context, modelName:String) : STMModelling {

    override val managedObjectModel: STMManagedObjectModel

    override val concreteEntities: Map<String, STMEntityDescription>

    init {
        managedObjectModel = createModel(context, modelName)
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

    private fun createModel(context: Context, modelName:String): STMManagedObjectModel {

        val assetManager = context.assets
        val stream = assetManager.open("model/$modelName.json")

        val scanner = Scanner(stream)

        val jsonModelString = StringBuilder()

        while (scanner.hasNext()) {
            jsonModelString.append(scanner.nextLine())
        }

        return STMManagedObjectModel(jsonModelString.toString())

    }

}
