package com.sistemium.sissales.base.session

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import com.sistemium.sissales.R
import com.sistemium.sissales.activities.ProfileActivity
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMFunctions
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import java.io.File
import java.util.*
import com.sistemium.sissales.base.classes.entitycontrollers.STMEntityController
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.BuildConfig


/**
 * Created by edgarjanvuicik on 05/02/2018.
 */

class STMCoreAuthController {

    companion object {

        var accessToken: String?
            get() {
                val prefStore = SecuredPreferenceStore.getSharedInstance()
                return prefStore.getString("accessToken", null)
            }
            set(value) {

                val prefStore = SecuredPreferenceStore.getSharedInstance()
                val property = prefStore.getString("accessToken", null)

                if (property != value){

                    prefStore.edit().putString("accessToken", value).apply()

                    if (value != null){
                        lastAuth = STMFunctions.stringFrom(Date())

                        tokenHash = STMFunctions.md5FromString(value)
                    }

                }

            }

        var modelEtag: String?
            get() {

                val prefStore = SecuredPreferenceStore.getSharedInstance()
                return prefStore.getString("modelEtag", null)

            }
            set(value) {

                val prefStore = SecuredPreferenceStore.getSharedInstance()

                prefStore.edit().putString("modelEtag", value).apply()

            }

        var tokenHash: String?
            get() {

                val prefStore = SecuredPreferenceStore.getSharedInstance()
                return prefStore.getString("tokenHash", null)

            }
            set(value) {

                val prefStore = SecuredPreferenceStore.getSharedInstance()

                prefStore.edit().putString("tokenHash", value).apply()

            }

        var rolesResponse: Map<*, *>?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                val rolesResponseJSON = prefStore?.getString("rolesResponse", null)

                return STMFunctions.gson.fromJson(rolesResponseJSON, Map::class.java)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                val rolesResponseJSON = STMFunctions.gson.toJson(value)

                prefStore?.edit()?.putString("rolesResponse", rolesResponseJSON)?.apply()

            }

        val configuration:String
            get() {

                if (BuildConfig.APPLICATION_ID.contains(".sissales")){

                    return "SisSales"

                }

                if (BuildConfig.APPLICATION_ID.contains(".driver")){

                    return "SisDriver"

                }

                if (BuildConfig.APPLICATION_ID.contains(".warehouse")){

                    return "SisWarehouse"

                }

                return "vfs"

            }

        val userAgent:String
            get() {

                return "i$configuration/${STMModelling.sharedModeler?.managedObjectModel?.userDefinedModelVersionIdentifier ?: "366"}"

            }

        var stcTabs: ArrayList<*>?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                val rolesResponseJSON = prefStore?.getString("stcTabs", null)

                return STMFunctions.gson.fromJson(rolesResponseJSON, ArrayList::class.java)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                val stcTabsResponseJSON = STMFunctions.gson.toJson(value)

                prefStore?.edit()?.putString("stcTabs", stcTabsResponseJSON)?.apply()

            }

        var accountOrg: String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("accountOrg", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("accountOrg", value)?.apply()

            }

        var iSisDB: String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("iSisDB", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("iSisDB", value)?.apply()

            }

        var requestID: String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("requestID", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("requestID", value)?.apply()

            }

        var entityResource: String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("entityResource", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("entityResource", value)?.apply()

            }

        var socketURL: String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("socketURL", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("socketURL", value)?.apply()

            }

        var userID: String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("userID", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("userID", value)?.apply()

            }

        var userName: String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("userName", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("userName", value)?.apply()

            }

        var phoneNumber: String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("phoneNumber", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("phoneNumber", value)?.apply()

            }

        var lastAuth: String?
            get() {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                return prefStore?.getString("lastAuth", null)

            }
            set(value) {

                val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

                prefStore?.edit()?.putString("lastAuth", value)?.apply()

            }

        val dataModelName: String
            get() {

                return configuration

            }

        fun requestNewSMSCode(phoneNumber: String): Promise<String?, Exception> {

            return task {

                val (_, _, result) = Fuel.get("https://api.sistemium.com/pha/auth", listOf("mobileNumber" to phoneNumber))
                        .responseJson()

                when (result) {
                    is Result.Success -> {

                        val id = result.get().obj().get("ID") as? String

                        requestID = id

                        this.phoneNumber = phoneNumber

                        return@task id

                    }

                    is Result.Failure -> {

                        if (result.error.localizedMessage.startsWith("java.net.UnknownHostException: Unable to resolve host")) {

                            throw Exception(MyApplication.appContext!!.getString(R.string.no_internet))

                        }

                        throw Exception(MyApplication.appContext!!.getString(R.string.wrong_phone))

                    }

                }

            }

        }

        fun requestAccessToken(id: String, smsCode: String): Promise<String, Exception> {

            return task {

                val (_, _, result) = Fuel.get("https://api.sistemium.com/pha/auth", listOf("ID" to id, "smsCode" to smsCode))
                        .header(mapOf("user-agent" to userAgent, "DeviceUUID" to STMFunctions.deviceUUID()))
                        .responseJson()

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

                    is Result.Failure -> {

                        if (result.error.localizedMessage.startsWith("java.net.UnknownHostException: Unable to resolve host")) {

                            throw Exception(MyApplication.appContext!!.getString(R.string.no_internet))

                        }

                        throw Exception(MyApplication.appContext!!.getString(R.string.wrong_sms))

                    }
                }

            }

        }

        @SuppressLint("PrivateResource")
        fun logIn(): Promise<Map<*, *>, Exception> {

            if (accessToken != null) {

                val myIntent = Intent(MyApplication.appContext, ProfileActivity::class.java)

                myIntent.putExtra("accessToken", accessToken)

                myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

                val options = ActivityOptions.makeCustomAnimation(MyApplication.appContext, R.anim.abc_fade_in, R.anim.abc_fade_out)

                return requestRoles() then {

                    MyApplication.appContext?.startActivity(myIntent, options.toBundle())

                    return@then it

                }

            }

            return task {

                throw Exception("no accessToken")

            }

        }

        private fun startSession() {

            STMFunctions.debugLog("STMCoreAuthController", "socketURL $socketURL")
            STMFunctions.debugLog("STMCoreAuthController", "entity resource $entityResource")

            STMSession.sharedSession!!.setupSyncer()

        }

        private fun requestRoles(): Promise<Map<*, *>, Exception> {

            if (BuildConfig.APPLICATION_ID.contains(".vfs")){

                return task {

                    val (_, _, result) = Fuel.get("https://oauth.it/api/roles", listOf("access_token" to accessToken))
                            .header(mapOf("user-agent" to userAgent, "DeviceUUID" to STMFunctions.deviceUUID(), "Authorization" to accessToken!!))
                            .responseJson()

                    if (result !is Result.Success) {

                        if (rolesResponse != null) {

                            startSession()

                            return@task rolesResponse!!

                        }

                        throw Exception("Wrong SMS Code")
                    }

                    val roles = STMFunctions.gson.fromJson(result.get().content, Map::class.java)

                    accountOrg = if (BuildConfig.APPLICATION_ID.contains("vfsDebug")) "vfsd" else "vfs"
                    userID = (roles["account"] as? Map<*, *>)?.get("id") as? String
                    userName = (roles["account"] as? Map<*, *>)?.get("name") as? String
                    socketURL = "https://socket3.sistemium.com/socket.io-client"
                    entityResource = "$accountOrg/Entity"
                    rolesResponse = roles
                    stcTabs = arrayListOf(hashMapOf(
                            "name" to "STMWKWebView",
                            "title" to "VFS",
                            "imageName" to "3colors-colorless.png",
                            "appManifestURI" to "https://vfsm2.sistemium.com/app.manifest",
                            "url" to "https://vfsm2.sistemium.com"
                    ))

                    return@task roles

                }

            } else {

                return task {

                    val (_, _, result) = Fuel.get("https://api.sistemium.com/pha/roles", listOf("access_token" to accessToken))
                            .header(mapOf("user-agent" to userAgent, "DeviceUUID" to STMFunctions.deviceUUID(), "Authorization" to accessToken!!))
                            .responseJson()

                    if (result !is Result.Success) {

                        if (rolesResponse != null) {

                            startSession()

                            return@task rolesResponse!!

                        }

                        throw Exception("Wrong SMS Code")
                    }

                    val roles = STMFunctions.gson.fromJson(result.get().content, Map::class.java)

                    rolesResponse = roles

                    accountOrg = (roles["roles"] as? Map<*, *>)?.get("org") as? String

                    iSisDB = (roles["roles"] as? Map<*, *>)?.get("iSisDB") as? String

                    val tabs = (roles["roles"] as? Map<*, *>)?.get("stcTabs") as? ArrayList<*>

                    if (tabs != null) {

                        stcTabs = tabs

                    } else {

                        val tab = (roles["roles"] as? Map<*, *>)?.get("stcTabs") as? Map<*, *>

                        if (tab != null) {

                            stcTabs = arrayListOf(tab)

                        }

                    }

                    startSession()

                    return@task roles
                }

            }

        }

    }

}