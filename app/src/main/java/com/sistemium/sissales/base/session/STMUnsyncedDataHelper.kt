package com.sistemium.sissales.base.session

import com.sistemium.sissales.activities.WebViewActivity
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMConstants.Companion.NOTIFICATION_SYNCER_HAVE_NO_UNSYNCED_OBJECTS
import com.sistemium.sissales.base.STMConstants.Companion.NOTIFICATION_SYNCER_HAVE_UNSYNCED_OBJECTS
import com.sistemium.sissales.base.STMConstants.Companion.STMPersistingOptionLts
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.classes.entitycontrollers.STMEntityController
import com.sistemium.sissales.interfaces.STMDataSyncing
import com.sistemium.sissales.interfaces.STMDataSyncingSubscriber
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.interfaces.STMSession
import com.sistemium.sissales.persisting.STMPredicate
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMUnsyncedDataHelper : STMDataSyncing {

    override var subscriberDelegate: STMDataSyncingSubscriber? = null
        set(value) {
            isPaused = true
            field = value
            if (value != null) subscribeUnsynced() else unsubscribeUnsynced()
        }
    var session: STMSession? = null
    private var isPaused = false
    private var syncingState = false
    private var erroredObjectsByEntity = hashMapOf<String, HashSet<String>>()
    private var subscriptions = arrayListOf<String>()

    override fun startSyncing() {

        isPaused = false

        startHandleUnsyncedObjects()

    }

    override fun pauseSyncing() {

        isPaused = true

    }

    override fun setSynced(success: Boolean, entityName: String, itemData: Map<*, *>, itemVersion: String): Boolean {

        if (!success) {

            STMFunctions.debugLog("STMUnsyncedDataHelper", "failToSync $entityName ${itemData["id"]}")

            declineFromSync(itemData, entityName)

        } else {

            val options = hashMapOf(STMPersistingOptionLts to itemVersion)

            session!!.persistenceDelegate.mergeSync(entityName, itemData, options)

        }

        sendNextUnsyncedObject()

        return true

    }

    override fun predicateForUnsyncedObjectsWithEntityName(entityName: String): STMPredicate? {

        val subpredicates = arrayListOf<STMPredicate>()

        if (entityName == "STMLogMessage") {

            val uploadLogType = session?.settingsController?.stringValueForSettings("uploadLog.type", "syncer")
            val logMessageSyncTypes = STMLogger.sharedLogger.syncingTypesForSettingType(uploadLogType).map {

                return@map STMPredicate("'$it'")

            }

            subpredicates.add(STMPredicate("type IN (${logMessageSyncTypes.joinToString(", ")})"))

            val date = Date()

            date.time -= STMConstants.LOGMESSAGE_MAX_TIME_INTERVAL_TO_UPLOAD

            subpredicates.add(STMPredicate("deviceCts > '${STMFunctions.stringFrom(date)}'"))

        }

        subpredicates.add(STMPredicate("deviceTs not null and (deviceTs > lts OR lts is null)"))

        return STMPredicate.combinePredicates(subpredicates)

    }


    private fun declineFromSync(obj: Map<*, *>, entityName: String) {

        val pk = obj[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as? String

        if (pk == null) {

            STMFunctions.debugLog("STMUnsyncedDataHelper", "have no object id")

            return

        }

        STMFunctions.debugLog("", "declineFromSync: $entityName $pk")

        if (erroredObjectsByEntity[entityName] == null) {

            erroredObjectsByEntity[entityName] = HashSet()

        }

        erroredObjectsByEntity[entityName]!!.add(pk)

    }

    private fun startHandleUnsyncedObjects() {

        if (subscriberDelegate == null || isPaused) {

            return checkUnsyncedObjects()

        }

        if (!syncingState) {

            syncingState = true
            sendNextUnsyncedObject()

        }

    }

    private fun anyObjectToSend(): Map<*, *>? {

        for (entityName in STMEntityController.sharedInstance.uploadableEntitiesNames!!) {

            val anyObjectToSend = findSyncableObjectWithEntityName(entityName)

            if (anyObjectToSend != null) {

                return hashMapOf(
                        "entityName" to entityName,
                        "object" to anyObjectToSend
                )

            }

        }

        return null

    }

    private fun sendNextUnsyncedObject() {

        if (!syncingState) {

            return finishHandleUnsyncedObjects()

        }

        val objectToSend = anyObjectToSend()

        sendUnsyncedObject(objectToSend)

    }

    private fun sendUnsyncedObject(objectToSend: Map<*, *>?) {

        if (objectToSend == null) {

            return finishHandleUnsyncedObjects()

        }

        val entityName = objectToSend["entityName"] as String
        val itemData = objectToSend["object"] as Map<*, *>

        STMFunctions.debugLog("STMUnsyncedDataHelper", "syncing entityName: $entityName xid:${itemData["id"]} ")

        val itemVersion = itemData[STMConstants.STMPersistingKeyVersion] as String

        subscriberDelegate?.haveUnsynced(entityName, itemData, itemVersion)

    }

    private fun finishHandleUnsyncedObjects() {

        syncingState = false
        checkUnsyncedObjects()
        initPrivateData()
        subscriberDelegate?.finishUnsyncedProcess()

    }

    private fun initPrivateData() {

        erroredObjectsByEntity = hashMapOf()

    }

    private val sentImportantErrors = arrayListOf<String>()

    private fun findSyncableObjectWithEntityName(entityName: String, exclude: ArrayList<String> = ArrayList()): Map<*, *>? {

        val unsyncedObject = unsyncedObjectWithEntityName(entityName, exclude) ?: return null

        val parentNames = STMModelling.sharedModeler!!.toOneRelationshipsForEntityName(entityName).values.filter {

            val column = it.relationshipName + STMConstants.RELATIONSHIP_SUFFIX

            val pk = unsyncedObject[column] as? String ?: return@filter false

            val find = session!!.persistenceDelegate
                .findSync(it.destinationEntityName, pk, null)

            if (find == null) {

                val errorMessage = "${it.destinationEntityName} have no id with $pk which is required by child $entityName with id ${unsyncedObject[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY]}"

                if (!sentImportantErrors.contains(errorMessage)){

                    sentImportantErrors.add(errorMessage)

                    STMLogger.sharedLogger.importantMessage(errorMessage)

                }

            }

            val lts = find?.get(STMConstants.STMPersistingOptionLts)

            lts == null || lts.toString().isEmpty()

        }

        if (parentNames.isNotEmpty()) {

            val nexExclude = ArrayList(exclude + unsyncedObject["id"] as String)

            return findSyncableObjectWithEntityName(entityName, nexExclude)

        }

        return unsyncedObject

    }

    private fun unsyncedObjectWithEntityName(entityName: String, exclude: ArrayList<String> = ArrayList()): Map<*, *>? {

        val subpredicates = arrayListOf<STMPredicate>()
        val unsyncedPredicate = predicateForUnsyncedObjectsWithEntityName(entityName)

        if (unsyncedPredicate != null) subpredicates.add(unsyncedPredicate)

        val predicate = STMPredicate.combinePredicates(subpredicates)

        val options = hashMapOf(
                STMConstants.STMPersistingOptionPageSize to 1,
                STMConstants.STMPersistingOptionOrder to "deviceTs,id",
                STMConstants.STMPersistingOptionOrderDirection to STMConstants.STMPersistingOptionOrderDirectionAscValue
        )

        return try {

            val result = session!!.persistenceDelegate.findAllSync(entityName, predicate, options)

            for (obj in result) {
                
                val pk = obj[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as String
                
                val isErrored = erroredObjectsByEntity[entityName]?.contains(pk)

                if (!exclude.contains(pk) && (isErrored == null || isErrored == false)) {

                    return obj

                }

            }

            null

        } catch (e: Exception) {

            STMFunctions.debugLog("STMUnsyncedDataHelper", e.toString())

            null

        }

    }

    private fun subscribeUnsynced() {

        subscriptions.forEach {

            session!!.persistenceDelegate.cancelSubscription(it)

        }

        subscriptions.clear()

        initPrivateData()

        STMEntityController.sharedInstance.persistenceDelegate = session!!.persistenceDelegate

        for (entityName in STMEntityController.sharedInstance.uploadableEntitiesNames!!) {

            val onlyLocalChanges = hashMapOf(STMPersistingOptionLts to false)

            val sid = session!!.persistenceDelegate.observeEntity(entityName, {

                val obj = it as Map<*, *>

                if (entityName == "STMLogMessage") {

                    //TODO not implemented

//                    val uploadLogType = session?.settingsController?.stringValueForSettings("uploadLog.type", "syncer")
//                    val logMessageSyncTypes = STMLogger.sharedLogger.syncingTypesForSettingType(uploadLogType).map {
//                        return@map STMPredicate("\"$it\"")
//                    }
//                    subpredicates.add(STMPredicate("IN", STMPredicate("type"), STMPredicate(", ", logMessageSyncTypes)))
//                    val date = Date()
//                    date.time -= STMConstants.LOGMESSAGE_MAX_TIME_INTERVAL_TO_UPLOAD
//                    subpredicates.add(STMPredicate(" > ", STMPredicate("deviceCts"), STMPredicate("\"${STMFunctions.stringFrom(date)}\"")))

                    return@observeEntity false

                }

                if (obj["deviceTs"] != null && (obj["deviceTs"] as String > obj["lts"] as String || obj["lts"] == null)) {
                    return@observeEntity true
                }

                return@observeEntity false

            }, onlyLocalChanges, {

                STMFunctions.debugLog("STMUnsyncedDataHelper", "observeEntity $entityName data count ${it.size}")

                startHandleUnsyncedObjects()

            })

            subscriptions.add(sid)

        }

    }

    private fun unsubscribeUnsynced() {

        STMFunctions.debugLog("STMUnsyncedDataHelper", "unsubscribeUnsynced")

        initPrivateData()

        checkUnsyncedObjects()

        finishHandleUnsyncedObjects()

    }

    private fun checkUnsyncedObjects(){

        val somethingToUpload = anyObjectToSend()

        val notificationName = if (somethingToUpload != null) NOTIFICATION_SYNCER_HAVE_UNSYNCED_OBJECTS else NOTIFICATION_SYNCER_HAVE_NO_UNSYNCED_OBJECTS

        WebViewActivity.webInterface?.postJSNotification(notificationName)

    }

}