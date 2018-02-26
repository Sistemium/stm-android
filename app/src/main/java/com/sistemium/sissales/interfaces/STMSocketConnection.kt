package com.sistemium.sissales.interfaces

import com.sistemium.sissales.enums.STMSocketEvent
import nl.komponents.kovenant.Deferred

/**
 * Created by edgarjanvuicik on 14/02/2018.
 */
interface STMSocketConnection {

    var isReady:Boolean

    fun socketSendEvent(event: STMSocketEvent, value:Any?): Deferred<Pair<Boolean, Array<*>?>, Exception>

}