package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMPersistingMergeInterceptor
import com.sistemium.sissales.interfaces.STMPersistingTransaction

/**
 * Created by edgarjanvuicik on 23/01/2018.
 */
class STMRecordStatusController :STMPersistingMergeInterceptor {

    override fun interceptedAttributes(attributes: Map<*, *>, options: Map<*, *>?, persistingTransaction: STMPersistingTransaction?): Map<*, *>? {

        if (options?.get(STMConstants.STMPersistingOptionRecordstatuses) == false) return attributes

        if (attributes["isRemoved"] == true && attributes["name"] != null){

            val objectXid = attributes["objectXid"]
            val entityNameToDestroy =  STMFunctions.addPrefixToEntityName(attributes["name"] as? String ?: throw Exception("record status without name"))

            val predicate = STMPredicate.primaryKeyPredicate(arrayOf(objectXid))

            if (persistingTransaction?.modellingDelegate?.isConcreteEntityName(entityNameToDestroy) == true)

                persistingTransaction.destroyWithoutSave(entityNameToDestroy, predicate, hashMapOf(STMConstants.STMPersistingOptionRecordstatuses to false))
        }

        if (attributes["isTemporary"] == true){

            return null

        }

        return attributes


    }

}