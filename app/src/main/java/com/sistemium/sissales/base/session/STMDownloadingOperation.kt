package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.calsses.entitycontrollers.STMClientEntityController

/**
 * Created by edgarjanvuicik on 24/11/2017.
 */

class STMDownloadingOperation(var entityName:String) :Runnable {

    private val lock = Object()

    override fun run() {

        STMFunctions.debugLog("STMDownloadingOperation", "start downloadEntityName: $entityName")

        var lastKnownEtag = STMClientEntityController.clientEntityWithName(entityName)["eTag"]

        if (lastKnownEtag == null) lastKnownEtag = "*"

        synchronized(lock){

            lock.wait()

        }

        TODO("not implemented")

//        [self.dowlonadingQueue.owner.dataDownloadingOwner receiveData:self.entityName offset:lastKnownEtag];

    }

    fun finish(){

        synchronized(lock){

            lock.notify()

        }

    }

}