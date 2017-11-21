package com.sistemium.sissales.persisting

import com.sistemium.sissales.interfaces.STMPersistingPromised
import com.sistemium.sissales.interfaces.STMPersistingRunning
import com.sistemium.sissales.interfaces.STMPersistingSync
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */
class STMPersister(val runner:STMPersistingRunning):STMPersistingSync,STMPersistingPromised {

    override fun findSync(entityName: String, identifier: String, options: Map<*, *>?): Map<*, *> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Array<Map<*, *>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun find(entityName: String, identifier: String, options: Map<*, *>?): Promise<Map<*, *>, Exception> {

        return task {

            return@task findSync(entityName, identifier, options)

        }

    }

    override fun findAll(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Promise<Array<Map<*, *>>, Exception> {

        return task{

            return@task findAllSync(entityName, predicate, options)

        }

    }

}