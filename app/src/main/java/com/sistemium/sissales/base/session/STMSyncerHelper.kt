package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMConstants.Companion.STM_ENTITY_NAME
import com.sistemium.sissales.calsses.entitycontrollers.STMEntityController
import com.sistemium.sissales.interfaces.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMSyncerHelper: STMDefantomizing, STMDataDownloading {
    
    override var downloadingQueue: ExecutorService? = null
    override var persistenceFantomsDelegate: STMPersistingFantoms? = null
    override var dataDownloadingOwner: STMDataDownloadingOwner? = null
    override var defantomizingOwner:STMDefantomizingOwner? = null

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

        }

    }

    override fun stopDownloading() {

        TODO("not implemented")

    }

}