package com.sistemium.sissales.model

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */
class STMAttributeDescription(attribute: Map<*,*>) {

    val attributeName:String = attribute["name"] as? String ?: throw Exception("Wrong attribute Name")

}