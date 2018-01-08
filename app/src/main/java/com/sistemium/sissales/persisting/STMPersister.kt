package com.sistemium.sissales.persisting

import com.sistemium.sissales.interfaces.STMPersistingPromised
import com.sistemium.sissales.interfaces.STMPersistingRunning
import com.sistemium.sissales.interfaces.STMPersistingSync
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */
class STMPersister(private val runner:STMPersistingRunning):STMPersistingSync,STMPersistingPromised {

    override fun findSync(entityName: String, identifier: String, options: Map<*, *>?): Map<*, *> {

        val predicate = STMPredicate.primaryKeyPredicate(arrayOf(identifier))

        val results = findAllSync(entityName, predicate, options)

        return results.first()

    }

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Array<Map<*, *>> {

        return runner.readOnly {
            return@readOnly it.findAllSync(entityName, predicate, options)
        }

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

    override fun mergeSync(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Map<*, *> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mergeManySync(entityName: String, attributeArray: ArrayList<*>, options: Map<*, *>?): Array<Map<*, *>> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun merge(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Promise<Map<*, *>, Exception> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mergeMany(entityName: String, attributeArray: ArrayList<*>, options: Map<*, *>?): Promise<Array<Map<*, *>>, Exception> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}