package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.calsses.entitycontrollers.STMEntityController
import com.sistemium.sissales.interfaces.STMDataSyncing
import com.sistemium.sissales.interfaces.STMDataSyncingSubscriber
import com.sistemium.sissales.interfaces.STMSession
import com.sistemium.sissales.persisting.STMPredicate
import java.util.*

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMUnsyncedDataHelper: STMDataSyncing {

    override var subscriberDelegate: STMDataSyncingSubscriber? = null
    var session:STMSession? = null
    private var isPaused = false
    private var syncingState = false
    private var erroredObjectsByEntity = hashMapOf<String, HashSet<String>>()
    private var pendingObjectsByEntity = hashMapOf<String, HashMap<String, ArrayList<*>>>()
    private var syncedPendingObjectsByEntity = hashMapOf<String, ArrayList<*>>()

    override fun startSyncing() {

        isPaused = false

        startHandleUnsyncedObjects()

    }

    override fun pauseSyncing() {

        isPaused = true

    }

    fun startHandleUnsyncedObjects(){

        if (subscriberDelegate == null || isPaused){

            //TODO
//            [self checkUnsyncedObjects];

        }
        if (!syncingState){

            syncingState = true
            sendNextUnsyncedObject()

        }

    }

    fun anyObjectToSend():Map<*,*>?{

        for (entityName in STMEntityController.sharedInstance.uploadableEntitiesNames!!){

            val anyObjectToSend = findSyncableObjectWithEntityName(entityName)

            if (anyObjectToSend != null){

                return hashMapOf(
                        "entityName" to entityName,
                        "object" to anyObjectToSend
                )

            }

        }

        return null

    }

    fun sendNextUnsyncedObject(){

        if (!syncingState){

            finishHandleUnsyncedObjects()

        }

        val objectToSend = anyObjectToSend()

        sendUnsyncedObject(objectToSend)

    }

    fun sendUnsyncedObject(objectToSend:Map<*,*>?){

        if (objectToSend == null) {

            return finishHandleUnsyncedObjects()

        }

        val entityName = objectToSend["entityName"] as String
        val itemData = objectToSend["object"] as Map<*,*>

        val itemVersion = STMConstants.STMPersistingKeyVersion

        subscriberDelegate!!.haveUnsynced(entityName, itemData, itemVersion)

    }

    fun finishHandleUnsyncedObjects(){

        syncingState = false
        initPrivateData()
        subscriberDelegate!!.finishUnsyncedProcess()

    }

    fun initPrivateData(){

        erroredObjectsByEntity = hashMapOf()
        pendingObjectsByEntity = hashMapOf()
        syncedPendingObjectsByEntity = hashMapOf()

    }

    fun findSyncableObjectWithEntityName(entityName:String):Map<*,*>?{

        val unsyncedObject = unsyncedObjectWithEntityName(entityName) ?: return null

        //TODO unsyncedParents
//        val unsyncedParents = checkUnsyncedParentsForObject(unsyncedObject, entityName)
//        if (unsyncedParents.count() > 0){
//
//            addPendingObject(unsyncedObject, entityName, ArrayList(unsyncedParents.values))
//
//            NSMutableDictionary *alteredObject = unsyncedObject.mutableCopy;
//            for (NSString *key in unsyncedParents.allKeys) {
//                alteredObject[key] = [NSNull null];
//            }
//            [alteredObject removeObjectForKey:STMPersistingKeyVersion];
//            return alteredObject.copy;
//
//        }

        return unsyncedObject

    }

    fun addPendingObject(obj:Map<*,*>, entityName:String, parents:ArrayList<*>){

        TODO("not implemented")

    }

    fun unsyncedObjectWithEntityName(entityName:String):Map<*,*>?{

        val subpredicates = arrayListOf<STMPredicate>()
        val unsyncedPredicate = predicateForUnsyncedObjectsWithEntityName(entityName)

        subpredicates.add(unsyncedPredicate)

        //TODO
//        NSPredicate *erroredExclusion = [self excludingErroredPredicateWithEntityName:entityName];
//        if (erroredExclusion) [subpredicates addObject:erroredExclusion];
//        NSPredicate *pendingObjectsExclusion = [self excludingPendingObjectsPredicateWithEntityName:entityName];
//        if (pendingObjectsExclusion) [subpredicates addObject:pendingObjectsExclusion];

        val predicate = STMPredicate.combinePredicates(subpredicates)

        val options = hashMapOf(
                STMConstants.STMPersistingOptionPageSize to 1,
                STMConstants.STMPersistingOptionOrder to "deviceTs,id",
                STMConstants.STMPersistingOptionOrderDirection to STMConstants.STMPersistingOptionOrderDirectionAscValue
        )

        return try{

            val result = session!!.persistenceDelegate.findAllSync(entityName, predicate, options)

            result.firstOrNull()
        }catch (e:Exception){

            STMFunctions.debugLog("STMUnsyncedDataHelper",e.toString())

            null

        }

    }

    fun predicateForUnsyncedObjectsWithEntityName(entityName:String):STMPredicate{

        val subpredicates = arrayListOf<STMPredicate>()

        if (entityName == "STMLogMessage"){

            val uploadLogType = session?.settingsController?.stringValueForSettings("uploadLog.type", "syncer")
            val logMessageSyncTypes = STMLogger.sharedLogger.syncingTypesForSettingType(uploadLogType).map {

                return@map STMPredicate("\"$it\"")

            }

            subpredicates.add(STMPredicate("IN", STMPredicate("type"), STMPredicate(", ", logMessageSyncTypes)))

            val date = Date()

            date.time -= STMConstants.LOGMESSAGE_MAX_TIME_INTERVAL_TO_UPLOAD

        subpredicates.add(STMPredicate(" > ", STMPredicate("deviceCts"), STMPredicate("\"${STMFunctions.stringFrom(date)}\"")))

        }

        subpredicates.add(STMPredicate("deviceTs not null and (deviceTs > lts OR lts is null)"))

        val predicate = STMPredicate.combinePredicates(subpredicates)

        return predicate

    }

    fun checkUnsyncedParentsForObject(obj:Map<*,*>?, entityName:String):Map<String, Map<*,*>>{

        TODO("not implemented")

//        BOOL hasUnsyncedParent = NO;
//        NSMutableDictionary <NSString *, NSDictionary *> *optionalUnsyncedParents = @{}.mutableCopy;
//        NSEntityDescription *entityDesciption = [self.persistenceDelegate entitiesByName][entityName];
//        NSArray *relNames = [self.persistenceDelegate toOneRelationshipsForEntityName:entityName].allKeys;
//        for (NSString *relName in relNames) {
//            NSString *relKey = [relName stringByAppendingString:RELATIONSHIP_SUFFIX];
//            NSString *parentId = object[relKey];
//            if ([STMFunctions isNull:parentId]) continue;
//            NSString *parentEntityName = [entityDesciption.relationshipsByName[relName] destinationEntity].name;
//            NSError *error;
//            NSDictionary *parent = [self.persistenceDelegate findSync:parentEntityName identifier:parentId options:nil error:&error];
//            if (!parent) {
//                if (error) {
//                    NSLog(@"error to find %@ %@: %@", parentEntityName, parentId, error.localizedDescription);
//                } else {
//                    NSLog(@"we have relation's id but have no both object with this id and error — something wrong with it");
//                }
//                continue;
//            }
//            BOOL theParentWasSynced = ![STMFunctions isEmpty:parent[STMPersistingOptionLts]];
//            if (theParentWasSynced || [self isSyncedPendingObject:parent entityName:parentEntityName]) {
//            continue;
//        }
//            hasUnsyncedParent = YES;
//            NSRelationshipDescription *relationship = entityDesciption.relationshipsByName[relName];
//            BOOL hasUnsyncedRequiredParent = relationship.inverseRelationship.deleteRule == NSCascadeDeleteRule;
//            BOOL wasOnceSynced = ![STMFunctions isEmpty:object[STMPersistingOptionLts]];
//            BOOL isSyncedPending = [self isSyncedPendingObject:object entityName:entityName];
//            if (hasUnsyncedRequiredParent || wasOnceSynced || isSyncedPending) {
//                return [NSDictionary dictionary];
//            }
//            optionalUnsyncedParents[relKey] = parent;
//        }
//        return hasUnsyncedParent ? optionalUnsyncedParents.copy : nil;

    }

}