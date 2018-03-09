package com.sistemium.sissales.model

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

class STMRelationshipDescription(relationship: Map<*,*>){

    val relationshipName:String = relationship["name"] as? String ?: throw Exception("Wrong relationship name")
    val destinationEntityName:String = relationship["destinationEntity"] as? String ?: throw Exception("Wrong destination entity name")
    val isToMany:Boolean = (relationship["toMany"] as? String) == "YES"
    val deleteRule:String = relationship["deletionRule"] as? String ?: throw Exception("Wrong relationship name")
    val inverseRelationshipName = relationship["inverseName"] as? String

}
