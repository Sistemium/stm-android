package com.sistemium.sissales.base.classes.entitycontrollers

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMConstants.Companion.STMPersistingOptionPageSize
import com.sistemium.sissales.base.STMConstants.Companion.STMPersistingOptionRecordstatuses
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreSessionManager
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.persisting.STMPredicate
import java.util.*

class STMCoreObjectsController {

    companion object {

        @JvmStatic
        fun checkObjectsForFlushing(){

            STMFunctions.debugLog("STMCoreObjectsController","checkObjectsForFlushing")

            val session = STMCoreSessionManager.sharedManager.currentSession ?: return

            val syncer = session.syncer ?: return

            val startFlushing = Date()

            val entitiesWithLifeTime = STMEntityController.entitiesWithLifeTime() ?: return

            for (entity in entitiesWithLifeTime) {

                val lifeTime = (entity["lifeTime"] as? Int)?.toLong() ?: continue

                val entityName = entity["name"] as? String ?: continue

                val terminatorDate = Date(startFlushing.time - lifeTime * 3600000)

                var dateField = entity["lifeTimeDateField"]

                if (dateField == null) {
                    dateField = "deviceCts"
                }

                val prefixedName = STMFunctions.addPrefixToEntityName(entityName)

                val datePredicate = STMPredicate("$dateField < '${STMFunctions.stringFrom(terminatorDate)}'")

                val unsyncedPredicate = syncer.dataSyncingDelegate?.predicateForUnsyncedObjectsWithEntityName(prefixedName)

                val subpredicates = arrayListOf(datePredicate)

                if (unsyncedPredicate != null){

                    subpredicates.add(STMPredicate("not ($unsyncedPredicate)"))

                }

                val relations = STMModelling.sharedModeler?.objectRelationshipsForEntityName(prefixedName,true, false) ?: continue

                val options = hashMapOf(
                        STMPersistingOptionRecordstatuses to false,
                        STMPersistingOptionPageSize to 1500
                )

                val denyCascades = arrayListOf<String>()

                relations.forEach {

                    val destinationName = STMFunctions.removePrefixFromEntityName(it.value.destinationEntityName)

                    if (it.value.deleteRule != "Deny"){

                        return@forEach

                    }

                    val concreteDescendants = STMModelling.sharedModeler!!.hierarchyForEntityName(it.value.destinationEntityName)

                    for (descendant in concreteDescendants){

                        val denyDelete = "not exists (select * from ${STMFunctions.removePrefixFromEntityName(descendant)} where ${it.value.inverseRelationshipName}Id = $entityName.id)"

                        denyCascades.add(denyDelete)

                    }

                    if (!STMModelling.sharedModeler!!.isConcreteEntityName(destinationName)) {

                        return@forEach

                    }

                    val denyDelete = "not exists (select * from $destinationName where ${it.value.inverseRelationshipName}Id = $entityName.id)"

                    denyCascades.add(denyDelete)

                }

                if (denyCascades.size > 0) {

                    options[STMConstants.STMPersistingOptionWhere] = denyCascades.joinToString(" AND ")

                }

                val predicate = STMPredicate.combinePredicates(subpredicates)

                val deletedCount = session.persistenceDelegate.destroyAllSync(entityName, predicate, options)

                STMFunctions.debugLog("STMCoreObjectsController","Flushed $deletedCount of $entityName")

            }

        }

    }

}