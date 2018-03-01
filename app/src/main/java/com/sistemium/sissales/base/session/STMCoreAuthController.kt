package com.sistemium.sissales.base.session

import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.sistemium.sissales.R
import com.sistemium.sissales.activities.WebViewActivity
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMFunctions
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then

/**
 * Created by edgarjanvuicik on 05/02/2018.
 */

//TODO cache vars with private vars?
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

        var rolesResponse:Map<*,*>?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                val rolesResponseJSON =  prefStore?.getString("rolesResponse", null)

                return STMFunctions.gson.fromJson(rolesResponseJSON, Map::class.java)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                val rolesResponseJSON =  STMFunctions.gson.toJson(value)

                prefStore?.edit()?.putString("rolesResponse", rolesResponseJSON)?.apply()

            }

        var stcTabs: ArrayList<*>?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                val rolesResponseJSON =  prefStore?.getString("stcTabs", null)

                return STMFunctions.gson.fromJson(rolesResponseJSON, ArrayList::class.java)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                val stcTabsResponseJSON =  STMFunctions.gson.toJson(value)

                prefStore?.edit()?.putString("stcTabs", stcTabsResponseJSON)?.apply()

            }

        var accountOrg:String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("accountOrg", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("accountOrg", value)?.apply()

            }

        var iSisDB:String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("iSisDB", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("iSisDB", value)?.apply()

            }

        var requestID:String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("requestID", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("requestID", value)?.apply()

            }

        var entityResource:String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("entityResource", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("entityResource", value)?.apply()

            }

        var socketURL:String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("socketURL", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("socketURL", value)?.apply()

            }

        var userID:String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("userID", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("userID", value)?.apply()

            }

        var userName:String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("userName", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("userName", value)?.apply()

            }

        val dataModelName = "iSisSales"

        fun requestNewSMSCode(phoneNumber:String):Promise<String?, Exception>{

            return task {

                val (_, _, result) = Fuel.get("https://api.sistemium.com/pha/auth", listOf("mobileNumber" to phoneNumber)).responseJson()

                when (result) {
                    is Result.Success -> {

                        val id = result.get().obj().get("ID") as? String

                        requestID = id

                        return@task id

                    }
                }

                throw Exception("Wrong phone number")
            }

        }

        fun requestAccessToken(id:String, smsCode:String):Promise<String, Exception>{

            return task {

                FuelManager.instance.baseHeaders = mapOf("user-agent" to "iSisSales/360")

                val (_,_, result) = Fuel.get("https://api.sistemium.com/pha/auth", listOf("ID" to id, "smsCode" to smsCode)).responseJson()

                when (result) {
                    is Result.Success -> {

                        val data = result.get().obj()

                        accessToken = data.get("accessToken") as? String
                        entityResource = data["redirectUri"] as? String
                        socketURL = data["apiUrl"] as? String
                        userID = data["ID"] as? String
                        userName = data["name"] as? String

                        return@task data.get("accessToken") as String

                    }
                }

                throw Exception("Wrong SMS Code")

            }

        }

        fun logIn():Promise<Map<*,*>, Exception>{

            val accessToken = STMCoreAuthController.accessToken

            if (accessToken != null){

                val myIntent = Intent(MyApplication.appContext, WebViewActivity::class.java)

                myIntent.putExtra("accessToken", accessToken)

                myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                val options = ActivityOptions.makeCustomAnimation(MyApplication.appContext, R.anim.abc_fade_in, R.anim.abc_fade_out)

                return requestRoles() then {

                    MyApplication.appContext?.startActivity(myIntent, options.toBundle())

                    return@then it

                } fail {
                    val test = ""
                }

            }

            return task {

                throw Exception("no accessToken")

            }

        }

        private fun startSession(){

            STMFunctions.debugLog("STMCoreAuthController", "socketURL $socketURL")
            STMFunctions.debugLog("STMCoreAuthController", "entity resource $entityResource")

            val trackers = arrayListOf("battery", "location")

            val sessionManager = STMCoreSessionManager.sharedManager

            sessionManager.startSession(trackers)

        }

        private fun requestRoles():Promise<Map<*,*>,Exception>{

            if (rolesResponse != null){

                return task {

                    startSession()

                    return@task rolesResponse!!

                }

            }

            return task {

                val (_,_, result) = Fuel.get("https://api.sistemium.com/pha/roles", listOf("access_token" to accessToken)).responseJson()

                when (result) {
                    is Result.Success -> {

                        val roles = STMFunctions.gson.fromJson(result.get().content, Map::class.java)

                        rolesResponse = roles

                        accountOrg = (roles["roles"] as? Map<*,*>)?.get("org") as? String

                        iSisDB = (roles["roles"] as? Map<*,*>)?.get("iSisDB") as? String

                        val tabs = (roles["roles"] as? Map<*,*>)?.get("stcTabs") as? ArrayList<*>

                        if (tabs != null){

                            stcTabs = tabs

                        }else{

                            val tab = (roles["roles"] as? Map<*,*>)?.get("stcTabs") as? Map<*,*>

                            if (tab != null) {

                                stcTabs = arrayListOf(tab)

                            }

                        }

                        startSession()

                        return@task roles

                    }
                }

                throw Exception("Wrong SMS Code")

            }

        }

    }

}