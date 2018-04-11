package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMFunctions
import java.util.concurrent.Executors

/**
 * Created by edgarjanvuicik on 05/03/2018.
 */
class STMSyncerHelperDefantomizing {

    val operationQueue = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    var failToResolveIds = ArrayList<Any>()

    @Volatile
    var operations = HashMap<Pair<String, String>, STMDefantomizingOperation>()

    fun addDefantomizationOfEntityName(entityName: String, identifier: String) {

        val op = STMDefantomizingOperation(entityName, identifier, STMCoreSessionManager.sharedManager.currentSession!!.syncer!!)

        try {

            operationQueue.execute(op)

            operations[Pair(entityName, identifier)] = op

        } catch (e: Exception) {

            STMFunctions.debugLog("STMSyncerHelperDefantomizing", "Rejecting defantomizing execution")

        }

    }

}