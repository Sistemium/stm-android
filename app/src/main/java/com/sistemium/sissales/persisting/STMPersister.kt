package com.sistemium.sissales.persisting

import com.sistemium.sissales.interfaces.*
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task

/**
 * Created by edgarjanvuicik on 20/11/2017.
 */
class STMPersister(private val runner:STMPersistingRunning):STMPersistingSync,STMPersistingPromised, STMPersistingIntercepting {

    override fun findSync(entityName: String, identifier: String, options: Map<*, *>?): Map<*, *> {

        val predicate = STMPredicate.primaryKeyPredicate(arrayOf(identifier))

        val results = findAllSync(entityName, predicate, options)

        return results.first()

    }

    override fun findAllSync(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): ArrayList<Map<*, *>> {

        return runner.readOnly {
            return@readOnly it.findAllSync(entityName, predicate, options)
        }

    }

    override fun find(entityName: String, identifier: String, options: Map<*, *>?): Promise<Map<*, *>, Exception> {

        return task {

            return@task findSync(entityName, identifier, options)

        }

    }

    override fun findAll(entityName: String, predicate: STMPredicate?, options: Map<*, *>?): Promise<ArrayList<Map<*, *>>, Exception> {

        return task{

            return@task findAllSync(entityName, predicate, options)

        }

    }

    override fun mergeSync(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Map<*, *> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun mergeManySync(entityName: String, attributeArray: ArrayList<*>, options: Map<*, *>?): ArrayList<Map<*, *>> {

        val result = arrayListOf<Map<*, *>>()

        this.runner.execute {

            for (attributes in attributeArray) {

                var merged: Map<*, *>? = applyMergeInterceptors(entityName, attributes as Map<*, *>, options, it) ?: continue

                merged = it.mergeWithoutSave(entityName, merged!!, options)

                if (merged != null) {

                    result += merged

                }
            }

            return@execute true

        }

//        notifyObservingEntityName(STMFunctions.addPrefixToEntityName(entityName), if (result.count() != 0)  result else _attributeArray, options)

        return result

    }

    override fun merge(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Promise<Map<*, *>, Exception> {

        return task{

            return@task mergeSync(entityName, attributes, options)

        }

    }

    override fun mergeMany(entityName: String, attributeArray: ArrayList<*>, options: Map<*, *>?): Promise<ArrayList<Map<*, *>>, Exception> {

        return task{

            return@task mergeManySync(entityName, attributeArray, options)

        }

    }

    //STMPersistingIntercepting

    override val beforeMergeInterceptors = hashMapOf<String, STMPersistingMergeInterceptor>()

    override fun beforeMergeEntityName(entityName: String, interceptor: STMPersistingMergeInterceptor?) {

        if (interceptor != null){

            beforeMergeInterceptors[entityName] = interceptor

        }else{

            beforeMergeInterceptors.remove(entityName)

        }

    }

    override fun applyMergeInterceptors(entityName:String, attributes:Map<*,*>, options:Map<*,*>?, persistingTransaction: STMPersistingTransaction?): Map<*, *>?{

        val interceptor = beforeMergeInterceptors[entityName] ?: return attributes

        return interceptor.interceptedAttributes(attributes,options,persistingTransaction)

    }

    //STMPersistingObservable

    private val subscriptions = hashMapOf<String, STMPersistingObservingSubscription>()

    fun notifyObservingEntityName(entityName:String, items: ArrayList<Map<*,*>>, options:Map<*,*>?){

        for (key in this.subscriptions.keys) {

            val subscription = this.subscriptions[key] ?: continue

            if (subscription.entityName == entityName) continue

//            NSSet *unmatchedOptions = [subscription.options keysOfEntriesPassingTest:^BOOL(NSString *optionName, id optionValue, BOOL *stop) {
//            if ([optionValue isKindOfClass:NSNumber.class]) {
//            //                if (![optionValue respondsToSelector:@selector(boolValue)] || ![options[optionName] respondsToSelector:@selector(boolValue)]) {
////                    return [optionValue isEqual:options[optionName]];
////                }
//            return [optionValue boolValue] != [(NSNumber *)options[optionName] boolValue];
//        }
//            return [optionValue isEqual:options[optionName]];
//        }];
//
//            if (unmatchedOptions.count) continue;
//
//            NSArray *itemsFiltered = items;
//
//            if (subscription.predicate) {
//                @try {
//                    itemsFiltered = [items filteredArrayUsingPredicate:subscription.predicate];
//                } @catch (NSException *exception) {
//                    NSLog(@"notifyObservingEntityName catch: %@", exception);
//                    itemsFiltered = nil;
//                }
//            }
//
//            if (!itemsFiltered.count) continue;
//
//            if (subscription.entityName) {
//                if (subscription.callback) subscription.callback(itemsFiltered);
//            } else {
//                if (subscription.callbackWithEntityName) subscription.callbackWithEntityName(entityName, itemsFiltered);
//            }

        }

        TODO("not implemented")

    }

}