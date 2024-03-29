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
class STMCoreSettingsController : STMPersistingMergeInterceptor, STMSettingsController {

    override var persistenceDelegate: STMFullStackPersisting? = null
        set(value) {

            if (value != null) {

                field = value

                currentSettings = field!!.findAllSync("STMSetting", null, null)

            }

        }

    private var currentSettings: ArrayList<Map<*, *>>? = null

    override fun currentSettingsForGroup(group: String): Map<*, *>? {

        val res = hashMapOf<Any, Any>()

        currentSettings?.forEach {

            if (it["group"] == group && it["name"] != null && it["value"] != null) {

                res[it["name"]!!] = it["value"]!!

            }

        }

        return res

    }

    override fun stringValueForSettings(settingsName: String, group: String): String? {

        return currentSettingsForGroup(group)?.get(settingsName) as? String

    }

    override fun interceptedAttributes(attributes: Map<*, *>, options: Map<*, *>?, persistingTransaction: STMPersistingTransaction?): Map<*, *>? {

        val setting = this.settingWithName(attributes["name"] as? String, attributes["group"] as? String)
                ?: return attributes

        val mutAtr = HashMap(attributes)

        mutAtr[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] = setting[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY]

        return mutAtr

    }

    private fun settingWithName(name: String?, group: String?): Map<*, *>? {

        return currentSettings!!.filter {

            return@filter group == it["group"] && name == it["name"]

        }.lastOrNull()
    }

}