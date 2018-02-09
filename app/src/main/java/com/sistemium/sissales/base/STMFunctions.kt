package com.sistemium.sissales.base

import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.util.Log
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by edgarjanvuicik on 14/11/2017.
 */

class STMFunctions{

    companion object {

        val gson = GsonBuilder().serializeNulls().create()

        fun addPrefixToEntityName(entityName:String):String {

            var _entityName = entityName

            if (!entityName.startsWith(STMConstants.ISISTEMIUM_PREFIX)) {
                _entityName = STMConstants.ISISTEMIUM_PREFIX + entityName
            }
            return _entityName

        }

        fun removePrefixFromEntityName(entityName:String):String {

            var _entityName = entityName

            if (entityName.startsWith(STMConstants.ISISTEMIUM_PREFIX)) {
                _entityName = entityName.removePrefix(STMConstants.ISISTEMIUM_PREFIX)
            }
            return _entityName

        }

        fun stringFromNow():String{

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

            sdf.timeZone = TimeZone.getTimeZone("GMT")

            return sdf.format(Date())


        }

        fun uuidString():String{

            return UUID.randomUUID().toString()

        }

        fun jsonStringFromObject(value:Any):String{

            TODO("not implemented")

        }

        fun debugLog(name:String, msg:String){

            Log.d("${stringFromNow()}:$name", msg)

        }

        fun isCorrectPhoneNumber(phoneNumber:String):Boolean{

            TODO("not implemented")

        }

        fun handleError(activity: Activity?, message:String){

            activity?.runOnUiThread{

                val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
                } else {
                    AlertDialog.Builder(activity)
                }
                builder.setTitle("Error")
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, { _, _ -> })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()

            }

        }
    }

}