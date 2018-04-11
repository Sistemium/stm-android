package com.sistemium.sissales.model

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */
class STMAttributeDescription(attribute: Map<*, *>) {

    val attributeName: String = attribute["name"] as? String
            ?: throw Exception("Wrong attribute Name")

    val attributeType: String = attribute["attributeType"] as? String
            ?: throw Exception("Wrong attribute Name")

    val indexed: Boolean = attribute["indexed"] as? String == "YES"

    override fun equals(other: Any?): Boolean {

        if (other !is STMAttributeDescription) {

            return false

        }

        return attributeName == other.attributeName

    }

    override fun hashCode(): Int {
        return attributeName.hashCode()
    }

}