package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMConstants.Companion.STMPersistingOptionLts
import com.sistemium.sissales.base.STMConstants.Companion.STM_ENTITY_NAME
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.calsses.entitycontrollers.STMClientEntityController
import com.sistemium.sissales.calsses.entitycontrollers.STMEntityController
import com.sistemium.sissales.interfaces.*
import nl.komponents.kovenant.then
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.collections.ArrayList

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMSyncerHelper: STMDefantomizing, STMDataDownloading {
    
    override var downloadingQueue: ExecutorService? = null
    override var persistenceFantomsDelegate: STMPersistingFantoms? = null
    override var dataDownloadingOwner: STMDataDownloadingOwner? = null
    override var defantomizingOwner:STMDefantomizingOwner? = null
    private var operations = hashMapOf<String, STMDownloadingOperation>()

    override fun startDownloading(entitiesNames:ArrayList<String>?) {

        var _entitiesNames = entitiesNames

        if (downloadingQueue != null){

            return

        }

        downloadingQueue = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

        if(_entitiesNames == null) {

            val names = hashSetOf(STM_ENTITY_NAME)
            names.addAll(STMEntityController.sharedInstance.downloadableEntityNames())

            if (STMEntityController.sharedInstance.entityWithName("STMSetting") != null){

                names.add("STMSetting")

            }

            _entitiesNames = ArrayList(names)

        }

        for (entityName in _entitiesNames){

            val operation = STMDownloadingOperation(entityName)

            operation.owner = this

            downloadingQueue?.execute(operation)

            operations[entityName] = operation

        }

    }

    override fun stopDownloading() {

        TODO("not implemented")

    }

    override fun dataReceivedSuccessfully(entityName: String, dataRecieved: ArrayList<*>?, offset: String?, pageSize: Int?, error:Exception?) {

        if (error != null){

            return doneDownloadingEntityName(entityName, error.localizedMessage)

        }

        var currentEtag = STMClientEntityController.clientEntityWithName(entityName)["eTag"]

        if (currentEtag == null){

            currentEtag = ""

        }

        if (dataRecieved!!.count() > 0 && offset!! == currentEtag) {

            STMClientEntityController.clientEntityWithName(entityName, offset)

        }

        if (dataRecieved.size == 0){

            doneDownloadingEntityName(entityName, null)

        }

        STMCoreSessionManager.sharedManager.currentSession!!.persistenceDelegate.mergeMany(entityName, dataRecieved, hashMapOf(STMPersistingOptionLts to STMFunctions.stringFrom(Date())))
                .then {

                    findAllResultMergedWithSuccess(dataRecieved, entityName, offset!!, pageSize!!)

                }

                .fail {

                    doneDownloadingEntityName(entityName, it.localizedMessage)

                }

    }

    override fun startDefantomization() {

//        STMSyncerHelperDefantomizing *defantomizing;
//        @synchronized (self) {
//            defantomizing = self.defantomizing;
//            if (!defantomizing) {
//                defantomizing = [STMSyncerHelperDefantomizing defantomizingWithDispatchQueue:self.dispatchQueue];
//                defantomizing.operationQueue.owner = self;
//            }
//            if (defantomizing.operationQueue.operationCount) return;
//            defantomizing.operationQueue.suspended = YES;
//            self.defantomizing = defantomizing;
//        }
//        for (NSString *entityName in [STMEntityController entityNamesWithResolveFantoms]) {
//            NSDictionary *entity = [STMEntityController stcEntities][entityName];
//            if (![STMFunctions isNotNull:entity[@"url"]]) {
//                NSLog(@"have no url for entity name: %@, fantoms will not to be resolved", entityName);
//                continue;
//            }
//            NSArray *results = [self.persistenceFantomsDelegate findAllFantomsIdsSync:entityName excludingIds:defantomizing.failToResolveIds.allObjects];
//            if (!results.count) continue;
//            NSLog(@"%@ %@ fantom(s)", @(results.count), entityName);
//            for (NSString *identifier in results)
//            [defantomizing.operationQueue addDefantomizationOfEntityName:entityName identifier:identifier];
//        }
//        NSUInteger count = defantomizing.operationQueue.operationCount;
//        if (!count) return [self defantomizingFinished];
//        NSLog(@"DEFANTOMIZING_START with queue of %@", @(count));
//        [self postAsyncMainQueueNotification:NOTIFICATION_DEFANTOMIZING_START
//                userInfo:@{@"fantomsCount": @(count)}];
//        defantomizing.operationQueue.suspended = NO;

//        TODO("not implemented")

    }

    override fun stopDefantomization() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun doneDownloadingEntityName(entityName:String, errorMessage:String?){

        if (errorMessage != null) {

            STMLogger.sharedLogger.errorMessage(errorMessage)

        }

        val operation = operations[entityName]

        operation?.finish()

        dataDownloadingOwner!!.dataDownloadingFinished()

    }

    private fun findAllResultMergedWithSuccess(result:ArrayList<*>, entityName:String, offset:String, pageSize:Int){

        STMFunctions.debugLog("STMSyncerHelper","    $entityName: got ${result.size} objects")

        if (result.size < pageSize){

            STMClientEntityController.clientEntityWithName(entityName, offset)
            return doneDownloadingEntityName(entityName, null)

        }

        dataDownloadingOwner!!.receiveData(entityName, offset)

    }

}