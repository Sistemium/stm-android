package com.sistemium.sissales.activities

import android.webkit.JavascriptInterface
import com.google.gson.Gson
import nl.komponents.kovenant.*
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.gson.GsonBuilder
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.persisting.STMPredicate


/**
 * Created by edgarjanvuicik on 27/10/2017.
 */

class WebAppInterface internal constructor(private var webViewActivity: WebViewActivity) {

    private val gson = GsonBuilder().serializeNulls().create()

    private val javascriptCallback = "iSistemiumIOSCallback"

    @JavascriptInterface
    fun errorCatcher(parameters: String?){

        Log.d("DEBUG", "errorCatcher")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        var description = mapParameters["description"]

        // TODO Save log Message needs to be implemented

    }

    @JavascriptInterface
    fun post(parameters: String?){

        Log.d("DEBUG", "post")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO is post needed?

    }

    @JavascriptInterface
    fun get(parameters: String?){

        Log.d("DEBUG", "got")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO is get needed?

    }

    @JavascriptInterface
    fun barCodeScannerOn(parameters: String?){

        Log.d("DEBUG", "barCodeScannerOn")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        var scannerScanJSFunction = mapParameters["scanCallback"]
        var scannerPowerButtonJSFunction = mapParameters["powerButtonCallback"]

        // TODO implement startBarcodeScanning

    }

    @JavascriptInterface
    fun findAll(parameters: String?){

        Log.d("DEBUG", "findAll")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        arrayOfObjectsRequestedByScriptMessage(mapParameters) then {

            result -> javascriptCallback(result, mapParameters)

        } fail {

            error -> javascriptCallback("$error", mapParameters)

        }

    }

    @JavascriptInterface
    fun find(parameters: String?){

        Log.d("DEBUG", "find")

        return findAll(parameters)

    }

    @JavascriptInterface
    fun updateAll(parameters: String?){

        Log.d("DEBUG", "updateAll")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        task {

            updateObjectsFromScriptMessage(mapParameters)

        } then {

            result -> javascriptCallback(result, mapParameters)

        } fail {

            error -> javascriptCallback("$error", mapParameters)

        }

    }

    @JavascriptInterface
    fun update(parameters: String?){

        Log.d("DEBUG", "update")

        return updateAll(parameters)

    }

    @JavascriptInterface
    fun destroy(parameters: String?){

        Log.d("DEBUG", "destroy")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        task {

            // TODO implement destroyObjectFromScriptMessage

            arrayOf<Map<*, *>>()

        } then {

            result -> javascriptCallback(result, mapParameters)

        } fail {

            error -> javascriptCallback("$error", mapParameters)

        }

    }

    @JavascriptInterface
    fun sound(parameters: String?){

        Log.d("DEBUG", "sound")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement destroyObjectFromScriptMessage

        javascriptCallback( arrayOf("didFinishSpeaking"), null, mapParameters["callBack"] as String)

    }

    @JavascriptInterface
    fun tabbar(parameters: String?){

        Log.d("DEBUG", "tabbar")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement handleTabbarMessage

        return if (mapParameters["action"]?.equals("show") == true){
            javascriptCallback(arrayOf("tabbar show success"), mapParameters)
        }else{
            javascriptCallback(arrayOf("tabbar hide success"), mapParameters)
        }

    }

    @JavascriptInterface
    fun subscribe(parameters: String?){

        Log.d("DEBUG", "subscribe")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement receiveSubscribeMessage

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("subscribe to entities success"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun remoteControl(parameters: String?){

        Log.d("DEBUG", "remoteControl")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement receiveRemoteCommands

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("remoteCommands ok"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun roles(parameters: String?){

        Log.d("DEBUG", "roles")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        val callback = mapParameters["callback"]

        if (webViewActivity.roles != null){

            return javascriptCallback(arrayOf(gson.fromJson(webViewActivity.roles!!, Map::class.java)), mapParameters, callback as String)

        }

        Fuel.get("https://api.sistemium.com/pha/roles", listOf("access_token" to webViewActivity.accessToken))
                .responseString { _, _, result ->

                    when (result) {
                        is Result.Failure -> {

                            val error:Error? = result.getAs()

                            javascriptCallback(error?.toString() ?: "" , mapParameters)

                        }
                        is Result.Success -> {

                            webViewActivity.roles = result.get()

                            javascriptCallback(arrayOf(gson.fromJson(result.get(), Map::class.java)), mapParameters, callback as String)

                        }
                    }

                }

    }

    @JavascriptInterface
    fun checkin(parameters: String?){

        Log.d("DEBUG", "checkin")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement checkinWithAccuracy

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun getPicture(parameters: String?){

        Log.d("DEBUG", "getPicture")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement getPictureWithEntityName

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("no image data"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun takePhoto(parameters: String?){

        Log.d("DEBUG", "takePhoto")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement saveImage

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun unsyncedInfoService(parameters: String?){

        Log.d("DEBUG", "unsyncedInfoService")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement unsyncedInfoService

    }

    @JavascriptInterface
    fun sendToCameraRoll(parameters: String?){

        Log.d("DEBUG", "sendToCameraRoll")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement loadImageForPrimaryKey

        val callback = mapParameters["callback"]

        return javascriptCallback("", mapParameters, callback as String)

    }

    @JavascriptInterface
    fun loadImage(parameters: String?){

        Log.d("DEBUG", "loadImage")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement loadImageForPrimaryKey

        val callback = mapParameters["callback"]

        return javascriptCallback("", mapParameters, callback as String)

    }

    @JavascriptInterface
    fun saveImage(parameters: String?){

        Log.d("DEBUG", "saveImage")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement saveImage

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun copyToClipboard(parameters: String?){

        Log.d("DEBUG", "copyToClipboard")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement copyToClipboard

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf<Any>(), mapParameters, callback as String)

    }

    // interface handling helpers

    private fun arrayOfObjectsRequestedByScriptMessage(parameters: Map<*, *>):Promise<Array<Map<*, *>>, Exception>{

        val entityName = parameters["entity"] as? String ?: throw Exception("entity is not specified")

        val xidString = parameters[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as? String

        val options = parameters["options"] as? Map<*,*>

        if (xidString != null){
            return webViewActivity.persistenceDelegate?.find(entityName, xidString, options)?.then {

                result ->

                return@then arrayOf(result)

            } ?: throw Exception("Missing persistenceDelegate")
        }

        val filter = parameters["filter"] as? Map<*, *>

        val where = parameters["where"] as? Map<*, *>

        val predicate = STMPredicate.filterPredicate(filter, where)

        return webViewActivity.persistenceDelegate?.findAll(entityName, predicate, options) ?: throw Exception("Missing persistenceDelegate")

    }

    private fun updateObjectsFromScriptMessage(parameters: Map<*, *>):Promise<Array<Map<*, *>>, Exception>{

        val entityName = parameters["entity"] as? String ?: throw Exception("entity is not specified")

        var parametersData = parameters["data"] as? ArrayList<*>

        if (parametersData == null){

            val map = parameters["data"] as? Map<*,*>

            if (map != null){

                parametersData = arrayListOf(map)

            }else{

                throw Exception("entity is not specified")

            }

        }

        return webViewActivity.persistenceDelegate?.mergeMany(entityName, parametersData, null) ?: throw Exception("Missing persistenceDelegate")

    }

    // callbacks

    private fun javascriptCallback(data:Any, parameters: Map<*, *>?, jsCallbackFunction: String){

        val arguments = mutableListOf<Any>()

        arguments.add(data)

        if (parameters is Map<*, *>){

            arguments.add(parameters)

        }

        val jsFunction = "window.$jsCallbackFunction && $jsCallbackFunction.apply(null,${this.gson.toJson(arguments)})"

        webViewActivity.webView?.post {

            webViewActivity.webView?.evaluateJavascript(jsFunction){
                result ->
                Log.d("DEBUG","EvaluateJS")
                Log.d("DEBUG", jsCallbackFunction)
                Log.d("DEBUG", this.gson.toJson(arguments))
                Log.d("DEBUG", result)
            }

        }

    }

    private fun javascriptCallback(data:Any, parameters: Map<*, *>?) =
            javascriptCallback(data, parameters, this.javascriptCallback)

    private fun javascriptCallback(error:String, parameters: Map<*, *>?){

        val arguments = mutableListOf<Any>()

        arguments.add(error)

        if (parameters is Map<*, *>){

            arguments.add(parameters)

        }

        val jsFunction = "${this.javascriptCallback}.apply(null, ${this.gson.toJson(arguments)})"

        webViewActivity.webView?.post {

            webViewActivity.webView?.evaluateJavascript(jsFunction){
                result ->
                Log.d("DEBUG","EvaluateErrorJS")
                Log.d("DEBUG", this.gson.toJson(arguments))
                Log.d("DEBUG", result)
            }

        }

    }

}
