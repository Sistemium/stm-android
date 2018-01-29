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

    private val _storageForEntityName = hashMapOf<String, STMStorageType>()

    override fun storageForEntityName(entityName: String): STMStorageType {

        if (_storageForEntityName[entityName] != null){

            return _storageForEntityName[entityName]!!

        }

        _storageForEntityName[entityName] = STMStorageType.STMStorageTypeNone

        val prefixedEntityName = STMFunctions.addPrefixToEntityName(entityName)

        val entity = entitiesByName[prefixedEntityName] ?: return _storageForEntityName[entityName]!!

        val storeOption = entity.userInfo["STORE"]

        if (entity.abstract) {
            _storageForEntityName[entityName] = STMStorageType.STMStorageTypeAbstract

            return _storageForEntityName[entityName]!!
        }

        if (storeOption == null || storeOption == "FMDB" || storeOption == "STMSQLiteDatabaseOperation"){

            _storageForEntityName[entityName] = STMStorageType.STMStorageTypeSQLiteDatabase

            return _storageForEntityName[entityName]!!
        }

        return _storageForEntityName[entityName]!!

    }

    override fun isConcreteEntityName(entityName: String): Boolean {

        val type = storageForEntityName(entityName)

        return type != STMStorageType.STMStorageTypeNone && type != STMStorageType.STMStorageTypeAbstract

    }

    private var _fieldsForEntityName = hashMapOf<String, Map<String, STMAttributeDescription>>()

    override fun fieldsForEntityName(entityName: String): Map<String, STMAttributeDescription> {

        if (_fieldsForEntityName[entityName] != null){

            return _fieldsForEntityName[entityName]!!

        }

        val _entityName = STMFunctions.addPrefixToEntityName(entityName)

        _fieldsForEntityName[entityName] = entitiesByName[_entityName]?.attributesByName ?: hashMapOf()

        if (entitiesByName[_entityName]?.parentEntity != null){
            _fieldsForEntityName[entityName] = _fieldsForEntityName[entityName]!!.plus(fieldsForEntityName(entitiesByName[_entityName]?.parentEntity!!))

        }

        return _fieldsForEntityName[entityName]!!

    }

    private var _objectRelationshipsForEntityName = hashMapOf<String, Map<String, STMRelationshipDescription>>()

    override fun objectRelationshipsForEntityName(entityName: String, isToMany: Boolean?, cascade: Boolean?, optional: Boolean?): Map<String, STMRelationshipDescription> {

        if (_objectRelationshipsForEntityName["$entityName$isToMany$cascade$optional"] != null){

            return _objectRelationshipsForEntityName["$entityName$isToMany$cascade$optional"]!!

        }

        val _entityName = STMFunctions.addPrefixToEntityName(entityName)

        _objectRelationshipsForEntityName["$entityName$isToMany$cascade$optional"] = entitiesByName[_entityName]?.relationshipsByName ?: hashMapOf()

        if (entitiesByName[_entityName]?.parentEntity != null){
            _objectRelationshipsForEntityName["$entityName$isToMany$cascade$optional"] = _objectRelationshipsForEntityName["$entityName$isToMany$cascade$optional"]!!.plus(objectRelationshipsForEntityName(entitiesByName[_entityName]?.parentEntity!!))

        }

        return _objectRelationshipsForEntityName["$entityName$isToMany$cascade$optional"]!!

    }

    override fun toOneRelationshipsForEntityName(entityName: String): Map<String, String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun objectRelationshipsForEntityName(entityName: String, isToMany: Boolean?): Map<String, String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun hierarchyForEntityName(entityName: String): Set<String> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
