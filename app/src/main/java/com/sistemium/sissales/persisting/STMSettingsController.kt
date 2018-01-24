package com.sistemium.sissales.persisting

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.interfaces.STMPersistingMergeInterceptor
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import java.util.HashMap

/**
 * Created by edgarjanvuicik on 23/01/2018.
 */
class STMSettingsController :STMPersistingMergeInterceptor {

    fun settingWithName(name:String?, group:String?):Map<*,*>?{

        TODO("not implemented")

    }

    override fun interceptedAttributes(attributes: Map<*, *>, options: Map<*, *>?, persistingTransaction: STMPersistingTransaction?): Map<*, *>? {

        val setting = this.settingWithName(attributes["name"] as? String, attributes["group"] as? String) ?: return attributes

        val mutAtr = HashMap(attributes)

        mutAtr[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] = setting[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY]

        return mutAtr

    }

}