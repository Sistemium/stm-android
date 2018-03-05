package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.session.STMCoreSessionManager
import com.sistemium.sissales.interfaces.STMPersistingFantoms

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMPersisterFantoms:STMPersistingFantoms {

    override fun findAllFantomsIdsSync(entityName: String, excludingIds: ArrayList<*>):ArrayList<String> {

        val result =  STMCoreSessionManager.sharedManager.currentSession!!.persistenceDelegate.findAllSync(entityName, null, hashMapOf(STMConstants.STMPersistingOptionFantoms to true))

        val _result = ArrayList(result.map {

            return@map it["id"] as String

        })

        return ArrayList(_result.filter {

            return@filter !excludingIds.contains(it)

        })

    }

}