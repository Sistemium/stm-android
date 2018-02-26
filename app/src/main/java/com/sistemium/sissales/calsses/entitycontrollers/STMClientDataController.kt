package com.sistemium.sissales.calsses.entitycontrollers

import com.sistemium.sissales.base.session.STMCoreSessionManager
import com.sistemium.sissales.interfaces.STMFullStackPersisting

/**
 * Created by edgarjanvuicik on 20/02/2018.
 */
class STMClientDataController {

    companion object {

        var persistenceDelegate: STMFullStackPersisting = STMCoreSessionManager.sharedManager.currentSession!!.persistenceDelegate

        val clientData:Map<*,*>
        get() {

            val entityName = "STMClientData"

            val fetchResult = persistenceDelegate.findAllSync(entityName, null, null)

            var clientData = fetchResult.lastOrNull()

            if (clientData == null) {

                clientData = hashMapOf<Any,Any>()

            }

            return clientData

        }

    }


}