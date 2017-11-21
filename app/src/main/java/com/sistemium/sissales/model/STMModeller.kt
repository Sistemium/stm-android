package com.sistemium.sissales.model

import android.content.Context
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMModelling
import java.util.*

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */

class STMModeller(context: Context, modelName:String) : STMModelling {

    override val managedObjectModel: STMManagedObjectModel

    override val concreteEntities: Map<String, STMEntityDescription>

    override val entitiesByName: Map<String, STMEntityDescription>

    init {
        managedObjectModel = createModel(context, modelName)
        concreteEntities = hashMapOf()
        entitiesByName = managedObjectModel.entitiesByName
    }

    override fun storageForEntityName(entityName: String): STMStorageType {

        val prefixedEntityName = STMFunctions.addPrefixToEntityName(entityName)

        val entity = entitiesByName[prefixedEntityName] ?: return STMStorageType.STMStorageTypeNone

        val storeOption = entity.userInfo["STORE"]

        if (entity.abstract) return STMStorageType.STMStorageTypeAbstract

        if (storeOption == null || storeOption == "FMDB" || storeOption == "SQLiteDatabase"){
            return STMStorageType.STMStorageTypeSQLiteDatabase
        }

        return STMStorageType.STMStorageTypeNone

    }

    override fun isConcreteEntityName(entityName: String): Boolean {

        val type = storageForEntityName(entityName)

        return type != STMStorageType.STMStorageTypeNone && type != STMStorageType.STMStorageTypeAbstract

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
