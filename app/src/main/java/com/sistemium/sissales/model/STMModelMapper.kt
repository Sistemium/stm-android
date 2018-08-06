package com.sistemium.sissales.model

import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMModelMapping
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by edgarjanvuicik on 28/11/2017.
 */
class STMModelMapper(savedModel: STMManagedObjectModel?, override var destinationModel: STMManagedObjectModel) : STMModelMapping {

    override val addedEntities = ArrayList<STMEntityDescription>()

    override val addedAttributes = HashMap<String, ArrayList<STMAttributeDescription>>()

    override val addedRelationships = HashMap<String, ArrayList<STMRelationshipDescription>>()

    override val removedEntities = ArrayList<STMEntityDescription>()

    override val removedAttributes = HashMap<String, ArrayList<STMAttributeDescription>>()

    override val removedRelationships = HashMap<String, ArrayList<STMRelationshipDescription>>()

    var needToMigrate: Boolean = false

    init {

        if (savedModel != destinationModel) {

            for (entity in destinationModel.entitiesByName){

                if (savedModel!!.entitiesByName.keys.contains(entity.key)){

                    val savedEntity = savedModel.entitiesByName[entity.key]!!

                    val atr = ArrayList<STMAttributeDescription>()

                    for (attribute in entity.value.attributesByName.values){

                        if (savedEntity.attributesByName.keys.contains(attribute.attributeName)){

                            if (attribute != savedEntity.attributesByName[attribute.attributeName]){

                                TODO("not implemented")

                            }

                        } else {

                            atr.add(attribute)

                        }

                    }

                    if (atr.size > 0){

                        addedAttributes[entity.key] = atr

                    }

                    val rel = ArrayList<STMRelationshipDescription>()

                    for (relation in entity.value.relationshipsByName.values){

                        if (savedEntity.relationshipsByName.keys.contains(relation.relationshipName)){

                            if (relation != savedEntity.relationshipsByName[relation.relationshipName]){

                                TODO("not implemented")

                            }

                        } else {

                            rel.add(relation)

                        }

                    }

                    if (rel.size > 0){

                        addedRelationships[entity.key] = rel

                    }

                } else {

                    addedEntities.add(entity.value)

                }

            }

            for (entity in savedModel!!.entitiesByName){

                if (destinationModel.entitiesByName.keys.contains(entity.key)){

                    val savedEntity = destinationModel.entitiesByName[entity.key]!!

                    val atr = ArrayList<STMAttributeDescription>()

                    for (attribute in entity.value.attributesByName.values){

                        if (savedEntity.attributesByName.keys.contains(attribute.attributeName)){

                            if (attribute != savedEntity.attributesByName[attribute.attributeName]){

                                TODO("not implemented")

                            }

                        } else {

                            atr.add(attribute)

                        }

                    }

                    if (atr.size > 0){

                        removedAttributes[entity.key] = atr

                    }

                    val rel = ArrayList<STMRelationshipDescription>()

                    for (relation in entity.value.relationshipsByName.values){

                        if (savedEntity.relationshipsByName.keys.contains(relation.relationshipName)){

                            if (relation != savedEntity.relationshipsByName[relation.relationshipName]){

                                TODO("not implemented")

                            }

                        } else {

                            rel.add(relation)

                        }

                    }

                    if (rel.size > 0){

                        removedRelationships[entity.key] = rel

                    }

                } else {

                    removedEntities.add(entity.value)

                }

            }

            val changes = addedEntities.size + removedEntities.size + addedAttributes.size + removedAttributes.size +
                     + addedRelationships.size + removedRelationships.size

            if (changes > 0) {

                STMFunctions.debugLog("STMModelMapper", "ModelMapper need to migrate")

                needToMigrate = true

                STMFunctions.debugLog("STMModelMapper", "Summ of changes: $changes")

            }

        }

    }

}