package com.sistemium.sissales.base.classes.entitycontrollers

import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.persisting.STMPredicate

/**
 * Created by edgarjanvuicik on 26/02/2018.
 */
class STMClientEntityController {

    companion object {

        fun clientEntityWithName(entityName: String): Map<*, *> {

            val name = STMFunctions.removePrefixFromEntityName(entityName)

            val predicate = STMPredicate("name = '$name'")

            val result = STMSession.sharedSession!!.persistenceDelegate.findAllSync("STMClientEntity", predicate, null)

            var clientEntity = result.lastOrNull()

            if (clientEntity == null) {

                val eTag = STMEntityController.sharedInstance!!.entityWithName(name)?.get("eTag")

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

            STMSession.sharedSession!!.persistenceDelegate.mergeSync("STMClientEntity", clientEntity, null)

        }
    }

}