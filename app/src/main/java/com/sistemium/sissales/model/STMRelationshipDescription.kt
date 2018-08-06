package com.sistemium.sissales.model

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

class STMRelationshipDescription(relationship: Map<*, *>) {

    val relationshipName: String = relationship["name"] as? String
            ?: throw Exception("Wrong relationship name")
    val destinationEntityName: String = relationship["destinationEntity"] as? String
            ?: throw Exception("Wrong destination entity name")
    val isToMany: Boolean = (relationship["toMany"] as? String) == "YES"
    val deleteRule: String? = relationship["deletionRule"] as? String
    val inverseRelationshipName = relationship["inverseName"] as? String
    val optional: Boolean = (relationship["optional"] as? String) == "YES"

    override fun equals(other: Any?): Boolean {

        if (other !is STMRelationshipDescription) {

            return false

        }

        return relationshipName == other.relationshipName
                && destinationEntityName == other.destinationEntityName
                && isToMany == other.isToMany
                && deleteRule == other.deleteRule
                && inverseRelationshipName == other.inverseRelationshipName
                && optional == other.optional

    }

    override fun hashCode(): Int {
        var result = relationshipName.hashCode()
        result = 31 * result + destinationEntityName.hashCode()
        result = 31 * result + isToMany.hashCode()
        result = 31 * result + (deleteRule?.hashCode() ?: 0)
        result = 31 * result + (inverseRelationshipName?.hashCode() ?: 0)
        result = 31 * result + optional.hashCode()
        return result
    }

}
