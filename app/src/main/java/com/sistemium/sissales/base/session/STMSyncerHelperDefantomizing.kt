package com.sistemium.sissales.base.session

import java.util.concurrent.Executors

/**
 * Created by edgarjanvuicik on 05/03/2018.
 */
class STMSyncerHelperDefantomizing {

    val operationQueue = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())

    var failToResolveIds = ArrayList<Any>()

    var operations = HashMap<String, STMDefantomizingOperation>()

    fun addDefantomizationOfEntityName(entityName:String, identifier:String){

        val op = STMDefantomizingOperation(entityName, identifier)

        operations[entityName] = op

        operationQueue.execute(op)

    }

}