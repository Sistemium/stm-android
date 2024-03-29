package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.*
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */
class STMPersister(private val runner: STMPersistingRunning) : STMFullStackPersisting, STMPersistingIntercepting {
    override fun close() {
        runner.close()
    }

    @Throws(Exception::class)
    override fun findSync(entityName: String, identifier: String, options: Map<*, *>?): Map<*, *>? {

        val predicate = STMPredicate.primaryKeyPredicate(arrayOf(identifier))

        val results = findAllSync(entityName, predicate, options)

        return results.firstOrNull()

    }

    @Throws(Exception::class)
    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): ArrayList<Map<*, *>> {

        return runner.readOnly {
            return@readOnly it.findAllSync(entityName, predicate, options)
        }

    }

    override fun find(entityName: String, identifier: String, options: Map<*, *>?): Promise<Map<*, *>?, Exception> {

        return task {

            return@task findSync(entityName, identifier, options)

        }

    }

    override fun findAll(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Promise<ArrayList<Map<*, *>>, Exception> {

        return task {

            return@task findAllSync(entityName, predicate, options)

        }

    }

    @Throws(Exception::class)
    override fun mergeSync(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Map<*, *>? {

        return mergeManySync(entityName, arrayListOf(attributes), options).firstOrNull()

    }

    @Throws(Exception::class)
    override fun mergeManySync(entityName: String, attributeArray: ArrayList<*>, options: Map<*, *>?): ArrayList<Map<*, *>> {

        val result = arrayListOf<Map<*, *>>()

        this.runner.execute {

            for (attributes in attributeArray) {

                var merged: Map<*, *>? = applyMergeInterceptors(entityName, attributes as Map<*, *>, options, it)
                        ?: continue

                merged = it.mergeWithoutSave(entityName, merged!!, options)

                if (merged != null) {

                    result += merged

                }
            }

            return@execute true

        }

        val res = result.ifEmpty { attributeArray }

        notifyObservingEntityName(STMFunctions.addPrefixToEntityName(entityName), res, options)

        return result

    }

    override fun merge(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Promise<Map<*, *>?, Exception> {

        return task {

            return@task mergeSync(entityName, attributes, options)

        }

    }

    override fun mergeMany(entityName: String, attributeArray: ArrayList<*>, options: Map<*, *>?): Promise<ArrayList<Map<*, *>>, Exception> {

        return task {

            return@task mergeManySync(entityName, attributeArray, options)

        }

    }

    override fun destroy(entityName: String, identifier: String, options: Map<*, *>?): Promise<Boolean, Exception> {

        return task {

            return@task destroySync(entityName, identifier, options)

        }

    }

    @Throws(Exception::class)
    override fun destroySync(entityName: String, identifier: String, options: Map<*, *>?): Boolean {

        val deletedCount = destroyAllSync(entityName, STMPredicate("${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY} = '$identifier'"), options)

        return deletedCount > 0

    }

    @Throws(Exception::class)
    override fun destroyAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Int {

        var count = 0

        val recordStatuses: ArrayList<Any>? = arrayListOf()

        val recordStatusEntity = STMFunctions.addPrefixToEntityName("RecordStatus")

        runner.execute {

            var objects = arrayListOf<Map<*, *>>()

            if (options?.get(STMConstants.STMPersistingOptionRecordstatuses) == null || options[STMConstants.STMPersistingOptionRecordstatuses] == true) {

                objects = findAllSync(entityName, predicate, options)

            }

            count = it.destroyWithoutSave(entityName, predicate, options)

            for (obj in objects) {

                var recordStatus: Map<*, *>? = hashMapOf(

                        "objectXid" to obj[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY],
                        "name" to STMFunctions.removePrefixFromEntityName(entityName),
                        "isRemoved" to 1

                )

                recordStatus = it.mergeWithoutSave(recordStatusEntity, recordStatus!!, hashMapOf(STMConstants.STMPersistingOptionRecordstatuses to false))

                if (recordStatus != null) {

                    recordStatuses?.add(recordStatus)

                }

            }

            return@execute true

        }

        if (recordStatuses?.count() != null && recordStatuses.count() > 0) {

            notifyObservingEntityName(recordStatusEntity, recordStatuses, options)

        }

        return count

    }

    //STMPersistingIntercepting

    private val beforeMergeInterceptors = hashMapOf<String, STMPersistingMergeInterceptor>()

    override fun beforeMergeEntityName(entityName: String, interceptor: STMPersistingMergeInterceptor?) {

        if (interceptor != null) {

            beforeMergeInterceptors[entityName] = interceptor

        } else {

            beforeMergeInterceptors.remove(entityName)

        }

    }

    override fun applyMergeInterceptors(entityName: String, attributes: Map<*, *>, options: Map<*, *>?, persistingTransaction: STMPersistingTransaction?): Map<*, *>? {

        val interceptor = beforeMergeInterceptors[entityName] ?: return attributes

        return interceptor.interceptedAttributes(attributes, options, persistingTransaction)

    }

    //STMPersistingObservable

    private val subscriptions = ConcurrentHashMap<String, STMPersistingObservingSubscription>()

    override fun notifyObservingEntityName(entityName: String, items: ArrayList<*>, options: Map<*, *>?) {

        for (subscription in this.subscriptions.values) {

            if (subscription.entityName != entityName) continue

            val unmatchedOptions = subscription.options?.filter {

                if (it.value is Boolean) {

                    return@filter it.value != (options?.get(it.key) != null)

                }

                return@filter it.value == options?.get(it.key)

            }

            if (unmatchedOptions?.count() != null && unmatchedOptions.count() != 0) {

                continue

            }

            var _items = items

            if (subscription.predicate != null) {

                _items = ArrayList(items.filter(subscription.predicate!!))

            }

            _items = ArrayList(_items)

            if (_items.count() == 0) continue

            if (subscription.entityName != null) {

                subscription.callback?.invoke(_items)

            } else {

                subscription.callbackWithEntityName?.invoke(entityName, _items)

            }

        }

    }

    override fun observeEntity(entityName: String, predicate: ((Any) -> Boolean)?, options: Map<*, *>, callback: (ArrayList<*>) -> Unit): String {

        val subscription = STMPersistingObservingSubscription(entityName, options, predicate)

        subscription.callback = callback

        subscriptions[subscription.identifier] = subscription

        return subscription.identifier

    }

    override fun cancelSubscription(subscriptionId: String): Boolean {

        val result = subscriptions[subscriptionId] != null

        if (result) {

            subscriptions.remove(subscriptionId)

        }

        return result

    }

}