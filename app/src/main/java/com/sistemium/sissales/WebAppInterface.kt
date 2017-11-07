package com.sistemium.sissales

import android.app.ActivityOptions
import android.content.Intent
import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.google.gson.Gson
import nl.komponents.kovenant.*
import android.os.Build
import android.util.Log
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs


/**
 * Created by edgarjanvuicik on 27/10/2017.
 */

class WebAppInterface internal constructor(internal var webViewActivity: WebViewActivity) {

    private val gson = Gson()

    private val javascriptCallback = "iSistemiumIOSCallback"

    @JavascriptInterface
    fun errorCatcher(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        var description = mapParameters["description"]

        // TODO Save log Message needs to be implemented

    }

    @JavascriptInterface
    fun post(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO is post needed?

    }

    @JavascriptInterface
    fun get(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO is get needed?

    }

    @JavascriptInterface
    fun barCodeScannerOn(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        var scannerScanJSFunction = mapParameters["scanCallback"]
        var scannerPowerButtonJSFunction = mapParameters["powerButtonCallback"]

        // TODO implement startBarcodeScanning

    }

    @JavascriptInterface
    fun findAll(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        task {

            // TODO implement arrayOfObjectsRequestedByScriptMessage

            arrayOf<Map<*, *>>()

        } then {

            result -> javascriptCallback(result, mapParameters)

        } fail {

            error -> javascriptCallback("$error", mapParameters)

        }

    }

    @JavascriptInterface
    fun find(parameters: String?){

        return findAll(parameters)

    }

    @JavascriptInterface
    fun updateAll(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        task {

            // TODO implement updateObjectsFromScriptMessage

            arrayOf<Map<*, *>>()

        } then {

            result -> javascriptCallback(result, mapParameters)

        } fail {

            error -> javascriptCallback("$error", mapParameters)

        }

    }

    @JavascriptInterface
    fun update(parameters: String?){

        return updateAll(parameters)

    }

    @JavascriptInterface
    fun destroy(parameters: String?){

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

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement destroyObjectFromScriptMessage

        javascriptCallback( arrayOf("didFinishSpeaking"), null, mapParameters["callBack"] as String)

    }

    @JavascriptInterface
    fun tabbar(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement handleTabbarMessage

        if (mapParameters.get("action")?.equals("show") == true){
            return javascriptCallback(arrayOf("tabbar show success"), mapParameters)
        }else{
            return javascriptCallback(arrayOf("tabbar hide success"), mapParameters)
        }

    }

    @JavascriptInterface
    fun subscribe(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement receiveSubscribeMessage

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("subscribe to entities success"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun remoteControl(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement receiveRemoteCommands

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("remoteCommands ok"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun roles(parameters: String?){

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

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement checkinWithAccuracy

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun getPicture(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement getPictureWithEntityName

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("no image data"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun takePhoto(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement saveImage

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun unsyncedInfoService(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement unsyncedInfoService

    }

    @JavascriptInterface
    fun sendToCameraRoll(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement loadImageForPrimaryKey

        val callback = mapParameters["callback"]

        return javascriptCallback("", mapParameters, callback as String)

    }

    @JavascriptInterface
    fun loadImage(parameters: String?){

        var mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement loadImageForPrimaryKey

        val callback = mapParameters["callback"]

        return javascriptCallback("", mapParameters, callback as String)

    }

    @JavascriptInterface
    fun saveImage(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement saveImage

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun copyToClipboard(parameters: String?){

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement copyToClipboard

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf<Any>(), mapParameters, callback as String)

    }

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
                Log.d("EvaluateJavascript", result)
                Log.d("ARGUMENTS", this.gson.toJson(arguments))
            }

        }

    }

    private fun javascriptCallback(data:Any, parameters: Map<*, *>?){

        return javascriptCallback(data, parameters, this.javascriptCallback)

    }

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
                Log.d("EvaluateJavascript", result)
                Log.d("ARGUMENTS", this.gson.toJson(arguments))
            }

        }

    }

}
