package com.sistemium.sissales.base.helper.logger

import android.util.Log
import com.sistemium.sissales.enums.STMLogMessageType
import com.sistemium.sissales.interfaces.STMSession

/**
 * Created by edgarjanvuicik on 31/01/2018.
 */
class STMLogger private constructor() {

    private object Holder { val INSTANCE = STMLogger() }

    companion object {
        val sharedLogger: STMLogger by lazy { Holder.INSTANCE }
    }

    var session: STMSession? = null

    private val availableTypes = arrayListOf("important", "error", "warning", "info", "debug")

    private val uploadLogType:String? = null
        get() {

            if (field == null){

                field = session?.settingsController!!.stringValueForSettings("uploadLog.type", "syncer")

            }

            return field

        }

    fun infoMessage(text:String){

        saveLogMessageWithText(text, STMLogMessageType.STMLogMessageTypeInfo)

    }

    fun errorMessage(text:String){

        saveLogMessageWithText(text, STMLogMessageType.STMLogMessageTypeError)

    }

    fun importantMessage(text:String){

        saveLogMessageWithText(text, STMLogMessageType.STMLogMessageTypeImportant)

    }

    fun syncingTypesForSettingType(settingType:String?):ArrayList<String>{

        val types = ArrayList(availableTypes)

        if (settingType == "debug"){
            return types
        } else {

            types.remove("debug")

            if (settingType == "debug"){

                return types

            } else {

                types.remove("debug")

                if (settingType == "info"){

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


    }

    private fun saveLogMessageWithText(text:String, numType: STMLogMessageType){

        var type = numType.toString()

        if (!availableTypes.contains(type)) type = "info"

        val uploadTypes = syncingTypesForSettingType(uploadLogType)

        if (uploadTypes.contains(type)){

            val logMessageDic = hashMapOf(
                    "text" to text,
                    "type" to type
            )

            nsLogMessageWithType(logMessageDic["type"], logMessageDic["text"])

            saveLogMessageDic(logMessageDic)

        }else{

            nsLogMessageWithType(type, text)

        }

    }

    private fun saveLogMessageDic(logMessageDic:Map<*,*>){

        TODO("not implemented")

    }

    private fun nsLogMessageWithType(type:String?, text:String?){

        Log.i("Log","$type, $text")

    }

}