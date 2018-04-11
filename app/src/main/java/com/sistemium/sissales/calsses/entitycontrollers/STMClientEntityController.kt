package com.sistemium.sissales.calsses.entitycontrollers

import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreSessionManager
import com.sistemium.sissales.persisting.STMPredicate

/**
 * Created by edgarjanvuicik on 26/02/2018.
 */
class STMClientEntityController {

    companion object {

        fun clientEntityWithName(entityName: String): Map<*, *> {

            val name = STMFunctions.removePrefixFromEntityName(entityName)

            val predicate = STMPredicate("=", STMPredicate("name"), STMPredicate("\"$name\""))

            val result = STMCoreSessionManager.sharedManager.currentSession!!.persistenceDelegate.findAllSync("STMClientEntity", predicate, null)

            if (result.count() > 1) {

                val logMessage = "more than one clientEntity with name $name"

                STMCoreSessionManager.sharedManager.currentSession!!.logger!!.errorMessage(logMessage)

            }

            var clientEntity = result.lastOrNull()

            if (clientEntity == null) {

                val eTag = STMEntityController.sharedInstance.entityWithName(name)?.get("eTag")

                clientEntity = hashMapOf(
                        "name" to name,
                        "eTag" to eTag
                )

            }

            return clientEntity
        }

        fun setEtag(name: String, eTag: String?) {

            val clientEntity = HashMap(clientEntityWithName(name))

            clientEntity["eTag"] = eTag

            STMCoreSessionManager.sharedManager.currentSession!!.persistenceDelegate.mergeSync("STMClientEntity", clientEntity, null)

        }
    }

}