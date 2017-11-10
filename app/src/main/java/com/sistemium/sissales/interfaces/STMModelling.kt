package com.sistemium.sissales.interfaces

import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.DataModel.STMAttributeDescription
import com.sistemium.sissales.DataModel.STMEntityDescription
import com.sistemium.sissales.DataModel.STMManagedObjectModel
import com.sistemium.sissales.DataModel.STMRelationshipDescription

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

interface STMModelling {

    val managedObjectModel: STMManagedObjectModel

    val concreteEntities: Map <String, STMEntityDescription>

    fun storageForEntityName(entityName:String): STMStorageType

    fun isConcreteEntityName(entityName:String): Boolean

    fun entitiesByName(entityName:String): Map<String, STMEntityDescription>

    fun fieldsForEntityName(entityName:String):Map<String, STMAttributeDescription>

    fun objectRelationshipsForEntityName(entityName:String, isToMany:Boolean, cascade:Boolean):Map<String, STMRelationshipDescription>

    fun objectRelationshipsForEntityName(entityName:String, isToMany:Boolean, cascade:Boolean, optional:Boolean):Map<String, STMRelationshipDescription>

    fun toOneRelationshipsForEntityName(entityName:String):Map<String, String>

    fun objectRelationshipsForEntityName(entityName:String, isToMany:Boolean):Map<String, String>

    fun hierarchyForEntityName(entityName:String):Set<String>

}