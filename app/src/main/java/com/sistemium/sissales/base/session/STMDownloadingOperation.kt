package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.calsses.entitycontrollers.STMClientEntityController

/**
 * Created by edgarjanvuicik on 24/11/2017.
 */

class STMDownloadingOperation(var entityName:String) :Runnable {

    var owner:STMSyncerHelper? = null

    private val lock = Object()

    override fun run() {

        STMFunctions.debugLog("STMDownloadingOperation", "start downloadEntityName: $entityName")

        var lastKnownEtag:String? = STMClientEntityController.clientEntityWithName(entityName)["eTag"] as? String

        if (lastKnownEtag == null) lastKnownEtag = "*"

        owner!!.dataDownloadingOwner!!.receiveData(entityName, lastKnownEtag)

        synchronized(lock){

            lock.wait()

        }

    }

    fun finish(){

        synchronized(lock){

            lock.notify()

        }

    }

}