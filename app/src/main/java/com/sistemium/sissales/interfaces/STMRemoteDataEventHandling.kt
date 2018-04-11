package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 14/02/2018.
 */
interface STMRemoteDataEventHandling {

    fun remoteUpdated(entityName: String, attributes: Map<*, *>)

    fun remoteHasNewData(entityName: String)

    fun remoteDestroyed(entityName: String, id: String)

}