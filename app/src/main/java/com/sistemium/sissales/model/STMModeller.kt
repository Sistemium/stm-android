package com.sistemium.sissales.model

import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMModelling

/**
 * Created by edgarjanvuicik on 10/11/2017.
 */

class STMModeller(modelJSON:String) : STMModelling {

    override val managedObjectModel: STMManagedObjectModel = STMManagedObjectModel(modelJSON)

    override val concreteEntities: Map<String, STMEntityDescription> = hashMapOf()

    override val entitiesByName: Map<String, STMEntityDescription> = managedObjectModel.entitiesByName

    override fun storageForEntityName(entityName: String): STMStorageType {

        val prefixedEntityName = STMFunctions.addPrefixToEntityName(entityName)

        val entity = entitiesByName[prefixedEntityName] ?: return STMStorageType.STMStorageTypeNone

        val storeOption = entity.userInfo["STORE"]

        if (entity.abstract) return STMStorageType.STMStorageTypeAbstract

        if (storeOption == null || storeOption == "FMDB" || storeOption == "STMSQLiteDatabaseOperation"){
            return STMStorageType.STMStorageTypeSQLiteDatabase
        }

        return STMStorageType.STMStorageTypeNone

    }

    override fun isConcreteEntityName(entityName: String): Boolean {

        val type = storageForEntityName(entityName)

        return type != STMStorageType.STMStorageTypeNone && type != STMStorageType.STMStorageTypeAbstract

    }

    override fun fieldsForEntityName(entityName: String): Map<String, STMAttributeDescription> {

        val _entityName = STMFunctions.addPrefixToEntityName(entityName)

        val current = entitiesByName[_entityName]?.attributesByName ?: hashMapOf()

        if (entitiesByName[_entityName]?.parentEntity != null){
            // TODO cashing
            return current.plus(fieldsForEntityName(entitiesByName[_entityName]?.parentEntity!!))

        }

        return current

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

}
