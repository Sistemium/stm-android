package com.sistemium.sissales.interfaces

import com.sistemium.sissales.persisting.STMPredicate

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
interface STMDataSyncing {

    fun startSyncing()
    fun pauseSyncing()

    fun setSynced(success: Boolean, entityName: String, itemData: Map<*, *>, itemVersion: String): Boolean

    fun predicateForUnsyncedObjectsWithEntityName(entityName: String): STMPredicate?

}