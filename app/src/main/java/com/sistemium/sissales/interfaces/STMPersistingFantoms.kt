package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
interface STMPersistingFantoms {

    fun findAllFantomsIdsSync(entityName:String, excludingIds:ArrayList<*>):ArrayList<String>

}