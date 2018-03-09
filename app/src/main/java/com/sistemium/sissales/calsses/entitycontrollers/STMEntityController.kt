package com.sistemium.sissales.calsses.entitycontrollers

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.interfaces.STMDataDownloadingOwner
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

    var persistenceDelegate: STMFullStackPersisting? = null
    set(value) {
        if (field == null && value != null){

            field = value
            field?.observeEntity(STMConstants.STM_ENTITY_NAME, null, hashMapOf<Any,Any>()){

                stcEntities = null
                entitiesArray = null
                uploadableEntitiesNames = null

                owner!!.entitiesChanged()

                STMFunctions.debugLog("STMEntityController", "checkStcEntities got called back with ${it.count()} items")
            }

        }
    }

    var owner: STMDataDownloadingOwner? = null

    var stcEntities:Map<String,Map<*,*>>? = null
    get() {

        if (field == null){

            val stc = hashMapOf<String, Map<*,*>>()

            entitiesArray?.forEach{

                val entityName = it["name"] as? String

                val capEntityName = entityName?.capitalize()

                if (capEntityName != null){

                    stc[STMFunctions.addPrefixToEntityName(entityName)] = it

                }

            }

            field = if (stc.count() > 0) stc else null


        }

        return field

    }

    var uploadableEntitiesNames:ArrayList<String>? = null
        get() {

            if (field == null){

                val filteredKeys = arrayListOf<String>()

                stcEntities?.forEach{

                    if (it.value["isUploadable"] == true){

                        filteredKeys.add(it.key)

                    }

                }

                field = filteredKeys

            }

            return field

        }

    private var entitiesArray:ArrayList<Map<*,*>>? = null
        get() {

            if (field == null){

                val options = hashMapOf(STMConstants.STMPersistingOptionOrder to "name")
                val result = persistenceDelegate?.findAllSync(STMConstants.STM_ENTITY_NAME, null, options)

                field = if (result?.count() ?: 0 > 0) result else null

            }

            return field

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

            val newStcEntitiesArray = ArrayList(entitiesArray)

            newStcEntitiesArray.removeAll(duplicates)

            entitiesArray = newStcEntitiesArray
            val predicate = STMPredicate.primaryKeyPredicate(duplicates.map { return@map it["id"] }.toTypedArray())

            persistenceDelegate?.destroyAllSync(STMConstants.STM_ENTITY_NAME, predicate, hashMapOf(STMConstants.STMPersistingOptionRecordstatuses to false))

        }

        if (totalDuplicates == 0){

            STMLogger.sharedLogger.infoMessage("stc.entity duplicates not found")

        }

    }

    fun downloadableEntityNames():ArrayList<String> {

        return ArrayList(stcEntities!!.filter{
            return@filter it.value["url"] != null
        }.keys)

    }

    fun resourceForEntity(entityName:String):String{

        val entity = stcEntities?.get(entityName)

        return if (entity?.get("url") != null) entity["url"] as String else "${STMCoreAuthController.accountOrg}/${entity?.get("name")}"

    }

    fun entityWithName(name:String):Map<*,*>?{

        val _name = STMFunctions.removePrefixFromEntityName(name)
        val predicate = STMPredicate("=", STMPredicate("name"), STMPredicate("\"$_name\""))
        return persistenceDelegate?.findAllSync("STMEntity", predicate, null)?.lastOrNull()

    }

    fun entityNamesWithResolveFantoms():ArrayList<String>{

        val filteredKeys = stcEntities!!.filter {

            return@filter it.value["isResolveFantoms"] == true && it.value["url"] != null

        }.keys

        return ArrayList(filteredKeys)

    }

}