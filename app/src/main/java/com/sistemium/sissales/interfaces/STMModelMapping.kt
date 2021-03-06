package com.sistemium.sissales.interfaces

import com.sistemium.sissales.model.*
import java.util.*

/**
 * Created by edgarjanvuicik on 05/03/2018.
 */
interface STMModelMapping {

    var destinationModel: STMManagedObjectModel

    val addedEntities: ArrayList<STMEntityDescription>
    val addedAttributes: HashMap<String, ArrayList<STMAttributeDescription>>
    val addedRelationships: HashMap<String, ArrayList<STMRelationshipDescription>>
    val removedEntities: ArrayList<STMEntityDescription>
    val removedAttributes: HashMap<String, ArrayList<STMAttributeDescription>>
    val removedRelationships: HashMap<String, ArrayList<STMRelationshipDescription>>

}