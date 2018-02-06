package com.sistemium.sissales.base.session

import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.sistemium.sissales.R
import com.sistemium.sissales.activities.CodeConfirmActivity
import com.sistemium.sissales.activities.WebViewActivity
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMFunctions
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task

/**
 * Created by edgarjanvuicik on 05/02/2018.
 */
class STMCoreAuthController {

    companion object {

        var accessToken:String?
        get() {

            val prefStore = SecuredPreferenceStore.getSharedInstance()
            return prefStore.getString("accessToken", null)

        }
        set(value) {

            val prefStore = SecuredPreferenceStore.getSharedInstance()

            prefStore.edit().putString("accessToken", value).apply()

        }

        fun requestNewSMSCode(phoneNumber:String):Promise<String?, Exception>{

            return task {

                val (_, _, result) = Fuel.get("https://api.sistemium.com/pha/auth", listOf("mobileNumber" to phoneNumber)).responseJson()

                when (result) {
                    is Result.Success -> {

                        return@task result.get().obj().get("ID") as? String

                    }
                }

                throw Exception("Wrong phone number")
            }

        }

        fun requestAccessToken(id:String, smsCode:String):Promise<String, Exception>{

            return task {

                val (_,_, result) = Fuel.get("https://api.sistemium.com/pha/auth", listOf("ID" to id, "smsCode" to smsCode)).responseJson()

                when (result) {
                    is Result.Success -> {

                        val data = result.get().obj()

                        STMCoreAuthController.accessToken = data.get("accessToken") as? String

                        return@task data.get("accessToken") as String

                    }
                }

                throw Exception("Wrong SMS Code")

            }

        }

        fun logIn():Boolean{

            val accessToken = STMCoreAuthController.accessToken

            if (accessToken != null){

                val myIntent = Intent(MyApplication.appContext, WebViewActivity::class.java)

                myIntent.putExtra("accessToken", accessToken)

                myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                val options = ActivityOptions.makeCustomAnimation(MyApplication.appContext, R.anim.abc_fade_in, R.anim.abc_fade_out)

                MyApplication.appContext?.startActivity(myIntent, options.toBundle())

                return true

            } else {

                return false

            }

        }

    }

}