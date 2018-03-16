package com.sistemium.sissales.base.session

import java.util.concurrent.Executors

/**
 * Created by edgarjanvuicik on 05/03/2018.
 */
class STMSyncerHelperDefantomizing {

    val operationQueue = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    var failToResolveIds = ArrayList<Any>()

    var operations = HashMap<Pair<String, String>, STMDefantomizingOperation>()

    fun addDefantomizationOfEntityName(entityName:String, identifier:String){

        val op = STMDefantomizingOperation(entityName, identifier, STMCoreSessionManager.sharedManager.currentSession!!.syncer!!)

        operations[Pair(entityName, identifier)] = op

        operationQueue.execute(op)

    }

}