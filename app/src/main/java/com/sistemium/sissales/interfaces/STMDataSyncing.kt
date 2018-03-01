package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
interface STMDataSyncing {

    var subscriberDelegate:STMDataSyncingSubscriber?

    fun startSyncing()
    fun pauseSyncing()

    fun setSynced(success:Boolean, entityName:String, itemData:Map<*,*>, itemVersion:String):Boolean

}