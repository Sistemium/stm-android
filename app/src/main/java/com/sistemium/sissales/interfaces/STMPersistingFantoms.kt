package com.sistemium.sissales.interfaces

import nl.komponents.kovenant.Promise

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
interface STMPersistingFantoms {

    fun findAllFantomsIdsSync(entityName:String, excludingIds:ArrayList<*>):ArrayList<String>
    fun mergeFantomAsync(entityName:String, attributes:Map<*,*>) :Promise<Map<*,*>, Exception>
    fun destroyFantomSync(entityName:String, identifier:String)

}