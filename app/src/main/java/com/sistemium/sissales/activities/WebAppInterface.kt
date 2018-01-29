package com.sistemium.sissales.activities

import android.webkit.JavascriptInterface
import nl.komponents.kovenant.*
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.google.gson.GsonBuilder
import com.sistemium.sissales.WebInterface.STMWebAppInterfaceSubscription
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.persisting.STMPredicate


/**
 * Created by edgarjanvuicik on 27/10/2017.
 */

class WebAppInterface internal constructor(private var webViewActivity: WebViewActivity) {

    private val gson = GsonBuilder().serializeNulls().create()

    private val javascriptCallback = "iSistemiumIOSCallback"

    @JavascriptInterface
    fun errorCatcher(parameters: String?){

        STMFunctions.debugLog("DEBUG", "errorCatcher")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        var description = mapParameters["description"]

        // TODO Save log Message needs to be implemented

    }

    @JavascriptInterface
    fun post(parameters: String?){

        STMFunctions.debugLog("DEBUG", "post")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO is post needed?

    }

    @JavascriptInterface
    fun get(parameters: String?){

        STMFunctions.debugLog("DEBUG", "got")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO is get needed?

    }

    @JavascriptInterface
    fun barCodeScannerOn(parameters: String?){

        STMFunctions.debugLog("DEBUG", "barCodeScannerOn")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        var scannerScanJSFunction = mapParameters["scanCallback"]
        var scannerPowerButtonJSFunction = mapParameters["powerButtonCallback"]

        // TODO implement startBarcodeScanning

    }

    @JavascriptInterface
    fun findAll(parameters: String?){

        STMFunctions.debugLog("DEBUG", "findAll")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        arrayOfObjectsRequestedByScriptMessage(mapParameters) then {

            STMFunctions.debugLog("FINDALL", "arrayOfObjectsRequestedByScriptMessage finished executing callback")

            javascriptCallback(it, mapParameters)

        } fail {

            javascriptCallback("$it", mapParameters)

        }

    }

    @JavascriptInterface
    fun find(parameters: String?){

        STMFunctions.debugLog("DEBUG", "find")

        return findAll(parameters)

    }

    @JavascriptInterface
    fun updateAll(parameters: String?){

        STMFunctions.debugLog("DEBUG", "updateAll")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        updateObjectsFromScriptMessage(mapParameters) then {

            javascriptCallback(it, mapParameters)

        } fail {

            javascriptCallback("$it", mapParameters)

        }

    }

    @JavascriptInterface
    fun update(parameters: String?){

        STMFunctions.debugLog("DEBUG", "update")

        return updateAll(parameters)

    }

    @JavascriptInterface
    fun destroy(parameters: String?){

        STMFunctions.debugLog("DEBUG", "destroy")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        task {

            // TODO implement destroyObjectFromScriptMessage

            arrayOf<Map<*, *>>()

        } then {

            javascriptCallback(it, mapParameters)

        } fail {

            javascriptCallback("$it", mapParameters)

        }

    }

    @JavascriptInterface
    fun sound(parameters: String?){

        STMFunctions.debugLog("DEBUG", "sound")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement destroyObjectFromScriptMessage

        javascriptCallback( arrayOf("didFinishSpeaking"), null, mapParameters["callBack"] as String)

    }

    @JavascriptInterface
    fun tabbar(parameters: String?){

        STMFunctions.debugLog("DEBUG", "tabbar")

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

        STMFunctions.debugLog("DEBUG", "subscribe")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        val entities = mapParameters["entities"] as? ArrayList<*>

        var errorMessage:String? = null

        val dataCallback = mapParameters["dataCallback"] as? String

        val callback = mapParameters["callback"] as? String

        if (dataCallback == null) {
            errorMessage = "No dataCallback specified"
        }

        if (callback == null) {
            errorMessage = "No callback specified"
        }

        if (entities == null){

            errorMessage = "No entities specified"

        }

        if (errorMessage != null){

            return javascriptCallback(errorMessage, mapParameters)

        }

        try {

            subscribeToEntities(entities!!, dataCallback!!)

        }catch (e:Exception){

            return javascriptCallback(e.toString(), mapParameters)

        }

        javascriptCallback(arrayOf("subscribe to entities success"), mapParameters, callback!!)

    }

    @JavascriptInterface
    fun remoteControl(parameters: String?){

        STMFunctions.debugLog("DEBUG", "remoteControl")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement receiveRemoteCommands

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("remoteCommands ok"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun roles(parameters: String?){

        STMFunctions.debugLog("DEBUG", "remoteControl")

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

        STMFunctions.debugLog("DEBUG", "checkin")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement checkinWithAccuracy

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun getPicture(parameters: String?){

        STMFunctions.debugLog("DEBUG", "getPicture")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement getPictureWithEntityName

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("no image data"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun takePhoto(parameters: String?){

        STMFunctions.debugLog("DEBUG", "takePhoto")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement saveImage

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun unsyncedInfoService(parameters: String?){

        STMFunctions.debugLog("DEBUG", "unsyncedInfoService")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement unsyncedInfoService

    }

    @JavascriptInterface
    fun sendToCameraRoll(parameters: String?){

        STMFunctions.debugLog("DEBUG", "sendToCameraRoll")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement loadImageForPrimaryKey

        val callback = mapParameters["callback"]

        return javascriptCallback("", mapParameters, callback as String)

    }

    @JavascriptInterface
    fun loadImage(parameters: String?){

        STMFunctions.debugLog("DEBUG", "loadImage")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement loadImageForPrimaryKey

        val callback = mapParameters["callback"]

        return javascriptCallback("", mapParameters, callback as String)

    }

    @JavascriptInterface
    fun saveImage(parameters: String?){

        STMFunctions.debugLog("DEBUG", "saveImage")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement saveImage

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun copyToClipboard(parameters: String?){

        STMFunctions.debugLog("DEBUG", "copyToClipboard")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement copyToClipboard

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf<Any>(), mapParameters, callback as String)

    }

    // interface handling helpers

    private fun arrayOfObjectsRequestedByScriptMessage(parameters: Map<*, *>):Promise<ArrayList<Map<*, *>>, Exception>{

        val entityName = parameters["entity"] as? String ?: throw Exception("entity is not specified")

        val xidString = parameters[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as? String

        val options = parameters["options"] as? Map<*,*>

        if (xidString != null){
            return webViewActivity.persistenceDelegate?.find(entityName, xidString, options)?.then {

                return@then arrayListOf(it)

            } ?: throw Exception("Missing persistenceDelegate")
        }

        val filter = parameters["filter"] as? Map<*, *>

        val where = parameters["where"] as? Map<*, *>

        val predicate = STMPredicate.filterPredicate(filter, where)

        return webViewActivity.persistenceDelegate?.findAll(entityName, predicate, options) ?: throw Exception("Missing persistenceDelegate")

    }

    private fun updateObjectsFromScriptMessage(parameters: Map<*, *>):Promise<ArrayList<Map<*, *>>, Exception>{

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

    private val subscriptions = hashMapOf<String, STMWebAppInterfaceSubscription>()

    private fun subscribeToEntities(entities:ArrayList<*>, callbackName:String){

        var subscription = subscriptions[callbackName]

        if (subscription == null){

            subscription = STMWebAppInterfaceSubscription(callbackName)

        }

        for (entityName in entities){

            val prfixedEntityName = STMFunctions.addPrefixToEntityName(entityName as String)

            subscription.entityNames.add(prfixedEntityName)

            updateLtsOffsetForEntityName(entityName, subscription)

        }

        for (subscriptionID in subscription.persisterSubscriptions){

            webViewActivity.persistenceDelegate?.cancelSubscription(subscriptionID)

        }

        val persisterSubscriptions = hashSetOf<String>()

        val options = hashMapOf(STMConstants.STMPersistingOptionLts to true)

        for (entityName in subscription.entityNames){

            persisterSubscriptions.add(webViewActivity.persistenceDelegate!!.observeEntity(entityName, null, options){

                sendSubscribedBunchOfObjects(it, entityName)

                var lts:String? = null

                for (obj in it){

                    val objLts = (obj as? Map<*,*>)?.get(STMConstants.STMPersistingOptionLts) as? String

                    if (objLts != null){

                        if (lts == null){

                            lts = objLts

                        }else{

                            if (objLts > lts) {

                                lts = objLts

                            }

                        }

                    }

                }

            if (lts != null) {

                subscription.ltsOffset[entityName] = lts

            }

            })

        }

        subscription.persisterSubscriptions = persisterSubscriptions
        subscriptions[callbackName] = subscription

    }

    private fun sendSubscribedBunchOfObjects(objectsArray:ArrayList<*>, entityName:String){

        TODO("not implemented")

    }

    private fun updateLtsOffsetForEntityName(entityName:String, subscription:STMWebAppInterfaceSubscription){

        val options = hashMapOf(STMConstants.STMPersistingOptionPageSize to 1,
                                STMConstants.STMPersistingOptionOrder to STMConstants.STMPersistingOptionLts,
                                STMConstants.STMPersistingOptionOrderDirection to STMConstants.STMPersistingOptionOrderDirectionDescValue)

        val objects = webViewActivity.persistenceDelegate?.findAllSync(entityName, null, options)

        if (objects?.firstOrNull() != null){

            subscription.ltsOffset[entityName] = objects.first()[STMConstants.STMPersistingOptionLts] as String

        }

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

            STMFunctions.debugLog("DEBUG", "EvaluateJS")
            STMFunctions.debugLog("JSFUNCTION", jsFunction)

            webViewActivity.webView?.evaluateJavascript(jsFunction){

                STMFunctions.debugLog("DEBUG", "Evaluate finish")

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

            STMFunctions.debugLog("DEBUG", "EvaluateErrorJS")
            STMFunctions.debugLog("JSFUNCTION", jsFunction)

            webViewActivity.webView?.evaluateJavascript(jsFunction){

                STMFunctions.debugLog("DEBUG", "Evaluate finish")

            }

        }

    }

}
