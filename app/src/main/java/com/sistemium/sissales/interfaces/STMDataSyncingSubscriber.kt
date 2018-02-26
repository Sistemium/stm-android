package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
interface STMDataSyncingSubscriber {

    fun finishUnsyncedProcess()
    fun haveUnsynced(entityName:String, itemData: Map<*,*>, itemVersion:String)

}