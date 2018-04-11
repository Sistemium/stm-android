package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreSessionManager
import com.sistemium.sissales.interfaces.STMPersistingFantoms
import nl.komponents.kovenant.Promise
import java.util.*

/**
 * Created by edgarjanvuicik on 09/02/2018.
 */
class STMPersisterFantoms : STMPersistingFantoms {

    override fun findAllFantomsIdsSync(entityName: String, excludingIds: ArrayList<*>): ArrayList<String> {

        val result = STMCoreSessionManager.sharedManager.currentSession!!.persistenceDelegate.findAllSync(entityName, null, hashMapOf(STMConstants.STMPersistingOptionFantoms to true))

        val _result = ArrayList(result.map {

            return@map it["id"] as String

        })

        return ArrayList(_result.filter {

            return@filter !excludingIds.contains(it)

        })

    }

    override fun mergeFantomAsync(entityName: String, attributes: Map<*, *>): Promise<Map<*, *>, Exception> {

        return STMCoreSessionManager.sharedManager.currentSession!!.persistenceDelegate.merge(entityName, attributes, hashMapOf(STMConstants.STMPersistingOptionLts to STMFunctions.stringFrom(Date())))

    }

    override fun destroyFantomSync(entityName: String, identifier: String) {

        STMCoreSessionManager.sharedManager.currentSession!!.persistenceDelegate.destroySync(entityName, identifier, hashMapOf(STMConstants.STMPersistingOptionRecordstatuses to false))

    }

}