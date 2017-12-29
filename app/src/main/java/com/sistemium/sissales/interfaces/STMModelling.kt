package com.sistemium.sissales.interfaces

import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.model.STMAttributeDescription
import com.sistemium.sissales.model.STMEntityDescription
import com.sistemium.sissales.model.STMManagedObjectModel
import com.sistemium.sissales.model.STMRelationshipDescription

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

interface STMModelling {

    val managedObjectModel: STMManagedObjectModel

    val concreteEntities: Map <String, STMEntityDescription>

    val entitiesByName:Map<String, STMEntityDescription>

    fun storageForEntityName(entityName:String): STMStorageType

    fun isConcreteEntityName(entityName:String): Boolean

    fun fieldsForEntityName(entityName:String):Map<String, STMAttributeDescription>

    fun objectRelationshipsForEntityName(entityName:String, isToMany:Boolean? = null, cascade:Boolean? = null, optional:Boolean? = null):Map<String, STMRelationshipDescription>

    fun toOneRelationshipsForEntityName(entityName:String):Map<String, String>

    fun objectRelationshipsForEntityName(entityName:String, isToMany:Boolean?):Map<String, String>

    fun hierarchyForEntityName(entityName:String):Set<String>

}