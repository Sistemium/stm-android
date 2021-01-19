package com.sistemium.sissales.base.helper.logger

import android.util.Log
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMSession
import java.util.*

/**
 * Created by edgarjanvuicik on 31/01/2018.
 */
class STMLogger private constructor() {

    companion object {
        private var INSTANCE:STMLogger? = null

        var sharedLogger: STMLogger?
            get() {

                if (INSTANCE == null){

                    INSTANCE = STMLogger()

                }

                return INSTANCE!!

            }
            set(value) {

                INSTANCE = value

            }
    }

    var session: STMSession? = null

    private val availableTypes = arrayListOf("important", "error", "warning", "info", "debug")

    private var uploadLogType: String? = null
        get() {

            if (field == null) {

                field = session?.settingsController?.stringValueForSettings("uploadLog.type", "syncer")

            }

            return field

        }

    fun infoMessage(text: String) {

        STMFunctions.debugLog("test", text)


    }

    fun errorMessage(text: String) {


    }

    fun importantMessage(text: String) {


    }

    fun syncingTypesForSettingType(settingType: String?): ArrayList<String> {

        val types = ArrayList(availableTypes)

        if (settingType == "debug") {

            return types

        } else {

            types.remove("debug")

            if (settingType == "info") {

                return types

            } else {

                types.remove("info")

                if (settingType == "warning") {

                    return types

                } else {

                    types.remove("warning")

                    return if (settingType == "error") {

                        types

                    } else {

                        types.remove("error")
                        types

                    }

                }

            }

        }


    }

    fun saveLogMessageWithText(text: String) {


    }

    private fun saveLogMessageDic(logMessageDic: Map<*, *>) {

        createAndSaveLogMessageFromDictionary(logMessageDic)

    }

    private fun createAndSaveLogMessageFromDictionary(logMessageDic: Map<*, *>) {

        val options = hashMapOf(STMConstants.STMPersistingOptionReturnSaved to false)

        session!!.persistenceDelegate.merge("STMLogMessage", logMessageDic, options)

    }

    private fun nsLogMessageWithType(type: String?, text: String?) {

        Log.i("Log", "$type, $text")

    }

}