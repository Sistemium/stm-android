package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
interface STMDefantomizing {

    var persistenceFantomsDelegate: STMPersistingFantoms?
    var defantomizingOwner: STMDefantomizingOwner?

    fun stopDefantomization()
    fun startDefantomization()
    fun defantomizedEntityName(entityName: String, identifier: String, attributes: Map<*, *>?, error: Exception?)

}