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

        return arrayListOf()

//        val result = @[].mutableCopy;

//        attributeArray = [self applyMergeInterceptors:entityName attributeArray:attributeArray options:options error:error];
//
//        if (!attributeArray.count || *error) return attributeArray;
//
//        [self.runner execute:^BOOL(id <STMPersistingTransaction> transaction) {
//
//            for (NSDictionary *attributes in attributeArray) {
//
//            NSDictionary *merged = [self applyMergeInterceptors:entityName attributes:attributes options:options error:error inTransaction:transaction];
//
//            if (*error) return NO;
//            if (!merged) continue;
//
//            merged = [transaction mergeWithoutSave:entityName attributes:merged options:options error:error];
//
//            if (*error) return NO;
//            if (merged) [result addObject:merged];
//
//        }
//
//            return YES;
//
//        }];
//
//        if (*error) return nil;
//
//        [self notifyObservingEntityName:[STMFunctions addPrefixToEntityName:entityName]
//                ofUpdatedArray:result.count ? result : attributeArray
//                options:options];
//
//        return result.copy;

//
        TODO("not implemented")

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

}