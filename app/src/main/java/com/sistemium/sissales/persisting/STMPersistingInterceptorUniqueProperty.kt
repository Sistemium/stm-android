package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.interfaces.STMPersistingMergeInterceptor
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import java.util.HashMap

/**
 * Created by edgarjanvuicik on 22/01/2018.
 */
class STMPersistingInterceptorUniqueProperty: STMPersistingMergeInterceptor {

    var entityName:String? = null
    var propertyName:String? = null

    override fun interceptedAttributes(attributes: Map<*, *>, options: Map<*, *>?, persistingTransaction: STMPersistingTransaction?): Map<*, *>? {

        val value = attributes[this.propertyName] ?: throw Exception("$propertyName can not be null in STMPersisterInterceptorUniqueProperty")

        val predicate = STMPredicate("=",STMPredicate(propertyName!!),STMPredicate("'$value'"))

        val findOptions = hashMapOf(STMConstants.STMPersistingOptionPageSize to 1)

        val existing = persistingTransaction?.findAllSync(entityName!!, predicate, findOptions)?.firstOrNull()
                ?: return attributes

        val mutAtr = HashMap(attributes)

        mutAtr[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] = existing[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY]

        return mutAtr

    }

}