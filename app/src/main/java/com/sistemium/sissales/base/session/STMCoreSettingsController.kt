package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.interfaces.STMFullStackPersisting
import com.sistemium.sissales.interfaces.STMPersistingMergeInterceptor
import com.sistemium.sissales.interfaces.STMPersistingTransaction
import com.sistemium.sissales.interfaces.STMSettingsController
import java.util.*

/**
 * Created by edgarjanvuicik on 23/01/2018.
 */
class STMCoreSettingsController :STMPersistingMergeInterceptor, STMSettingsController {

    var persistenceDelegate: STMFullStackPersisting? = null

    private var _currentSettings:ArrayList<Map<*,*>>? = null
    private var currentSettings:ArrayList<Map<*,*>>? = null
    get() {

        if (_currentSettings == null || _currentSettings?.size == 0){

            reloadCurrentSettings()

        }

        return _currentSettings

    }

    private fun reloadCurrentSettings(){

        _currentSettings = persistenceDelegate?.findAllSync("STMSetting", null, null)

    }

    override fun currentSettingsForGroup(group: String): Map<*, *>? {

        val res = hashMapOf<Any,Any>()

        currentSettings?.forEach {

            if (it["group"] == group && it["name"] != null && it["value"] != null){

                res[it["name"]!!] = it["value"]!!

            }

        }

        return res

    }

    fun settingWithName(name:String?, group:String?):Map<*,*>?{

        TODO("not implemented")

    }

    override fun stringValueForSettings(settingsName: String, group: String): String? {

        return currentSettingsForGroup(group)?.get(settingsName) as String

    }

    override fun interceptedAttributes(attributes: Map<*, *>, options: Map<*, *>?, persistingTransaction: STMPersistingTransaction?): Map<*, *>? {

        val setting = this.settingWithName(attributes["name"] as? String, attributes["group"] as? String) ?: return attributes

        val mutAtr = HashMap(attributes)

        mutAtr[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] = setting[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY]

        return mutAtr

    }

}