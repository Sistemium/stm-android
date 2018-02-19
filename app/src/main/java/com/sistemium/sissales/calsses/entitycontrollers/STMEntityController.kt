package com.sistemium.sissales.calsses.entitycontrollers

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.interfaces.STMFullStackPersisting
import com.sistemium.sissales.persisting.STMPredicate

/**
 * Created by edgarjanvuicik on 12/02/2018.
 */
class STMEntityController private constructor() {

    private object Holder { val INSTANCE = STMEntityController() }

    companion object {

        val sharedInstance: STMEntityController by lazy { Holder.INSTANCE }

    }

    private var _stcEntities:Map<String,Map<*,*>>? = null

    var persistenceDelegate: STMFullStackPersisting? = null

    var stcEntities:Map<String,Map<*,*>>? = null
    get() {

        if (_stcEntities == null){

            val stc = hashMapOf<String, Map<*,*>>()

            entitiesArray?.forEach{

                val entityName = it["name"] as? String

                val capEntityName = entityName?.capitalize()

                if (capEntityName != null){

                    stc[STMFunctions.addPrefixToEntityName(entityName)] = it

                }

            }

            _stcEntities = if (stc.count() > 0) stc else null


        }

        return _stcEntities

    }

    var _entitiesArray:ArrayList<Map<*,*>>? = null

    var entitiesArray:ArrayList<Map<*,*>>? = null
    get() {

        if (_entitiesArray == null){

            val options = hashMapOf(STMConstants.STMPersistingOptionOrder to "name")
            val result = persistenceDelegate?.findAllSync(STMConstants.STM_ENTITY_NAME, null, options)

            _entitiesArray = if (result?.count() ?: 0 > 0) result else null

        }

        return _entitiesArray

    }

    fun checkEntitiesForDuplicates(){

        val names = entitiesArray?.map {

            return@map it["name"]

        }

        var totalDuplicates = 0

        names?.forEach {

            var result = entitiesArray?.filter {

                filterIt ->

                return@filter filterIt["name"] == it

            }

            if (result == null || result.size < 2){

                return

            }

            totalDuplicates += result.size - 1

            result = result.sortedWith(compareBy({ it["isFantom"] as String }, { it["deviceCts"] as String }))

            val duplicates = result.subList(1, result.size - 1)

            val message = "Entity $it have ${duplicates.size} duplicates"

            STMLogger.sharedLogger.errorMessage(message)

            val newStcEntitiesArray = ArrayList(_entitiesArray)

            newStcEntitiesArray.removeAll(duplicates)

            _entitiesArray = newStcEntitiesArray
            val predicate = STMPredicate.primaryKeyPredicate(duplicates.map { return@map it["id"] }.toTypedArray())

            persistenceDelegate?.destroyAllSync(STMConstants.STM_ENTITY_NAME, predicate, hashMapOf(STMConstants.STMPersistingOptionRecordstatuses to false))

        }

        if (totalDuplicates == 0){

            STMLogger.sharedLogger.infoMessage("stc.entity duplicates not found")

        }

    }

}