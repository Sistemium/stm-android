package com.sistemium.sissales.interfaces

import com.sistemium.sissales.enums.STMSocketEvent
import nl.komponents.kovenant.Promise

/**
 * Created by edgarjanvuicik on 14/02/2018.
 */
interface STMSocketConnection {

    var isReady:Boolean

    fun socketSendEvent(event: STMSocketEvent, value:Any?): Promise<Array<*>, Exception>

    fun mergeAsync(entityName:String, attributes:Map<*,*>, options:Map<*,*>?) : Promise<Map<*, *>, Exception>

    fun findAllAsync(entityName:String, options:Map<*,*>?): Promise<Map<*, *>, Exception>

}