package com.sistemium.sissales.model

import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMModelMapping
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by edgarjanvuicik on 28/11/2017.
 */
class STMModelMapper(savedModel:STMManagedObjectModel?, override var destinationModel:STMManagedObjectModel):STMModelMapping {

    override val addedEntities: ArrayList<STMEntityDescription> by lazy {

        //TODO
        return@lazy ArrayList(destinationModel.entitiesByName.values)

    }

    override val addedProperties: HashMap<String, ArrayList<STMPropertyDescription>> by lazy {

        //TODO
        return@lazy hashMapOf<String, ArrayList<STMPropertyDescription>>()

    }

    override val addedAttributes: HashMap<String, ArrayList<STMAttributeDescription>> by lazy {

        //TODO
        return@lazy hashMapOf<String, ArrayList<STMAttributeDescription>>()

    }

    override val addedRelationships: HashMap<String, ArrayList<STMRelationshipDescription>> by lazy {

        //TODO
        return@lazy hashMapOf<String, ArrayList<STMRelationshipDescription>>()

    }

    override val removedEntities: ArrayList<STMEntityDescription> by lazy {

        //TODO
        return@lazy arrayListOf<STMEntityDescription>()

    }

    override val removedProperties: HashMap<String, ArrayList<STMPropertyDescription>> by lazy {

        //TODO
        return@lazy hashMapOf<String, ArrayList<STMPropertyDescription>>()

    }

    override val removedAttributes: HashMap<String, ArrayList<STMAttributeDescription>> by lazy {

        //TODO
        return@lazy hashMapOf<String, ArrayList<STMAttributeDescription>>()

    }

    override val removedRelationships: HashMap<String, ArrayList<STMRelationshipDescription>> by lazy {

        //TODO
        return@lazy hashMapOf<String, ArrayList<STMRelationshipDescription>>()

    }

    var needToMigrate:Boolean = false

    init {

        if (savedModel != destinationModel){

            val changes = addedEntities.size + removedEntities.size + addedAttributes.size + removedAttributes.size +
                    addedProperties.size + removedProperties.size + addedRelationships.size + removedRelationships.size

            if (changes > 0) {

                STMFunctions.debugLog("STMModelMapper","ModelMapper need to migrate")

                needToMigrate = true

                STMFunctions.debugLog("STMModelMapper","Summ of changes: $changes")

            }

        }

    }

}