package com.sistemium.sissales.webInterface

import android.location.LocationManager
import android.webkit.JavascriptInterface
import com.sistemium.sissales.activities.WebViewActivity
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.STMFunctions.Companion.gson
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.interfaces.STMFullStackPersisting
import com.sistemium.sissales.persisting.STMPredicate
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import android.location.Criteria
import android.location.Location
import android.os.Bundle
import android.location.LocationListener
import com.google.android.gms.common.api.GoogleApiClient
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePhotosController
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePicturesController
import com.sistemium.sissales.base.session.STMSession

import java.util.*
import com.google.android.gms.location.LocationServices
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.location.LocationRequest
import android.annotation.SuppressLint
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult


/**
 * Created by edgarjanvuicik on 27/10/2017.
 */

class WebAppInterface internal constructor(private var webViewActivity: WebViewActivity) {

    private val javascriptCallback = "iSistemiumIOSCallback"

    private val errorCallback = "iSistemiumIOSErrorCallback"

    private var unsyncedInfoJSFunction:String? = null

    private var persistenceDelegate: STMFullStackPersisting = STMSession.sharedSession!!.persistenceDelegate

    @JavascriptInterface
    fun errorCatcher(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "errorCatcher")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        var description = mapParameters["description"]

        // TODO Save log Message needs to be implemented

    }

    @JavascriptInterface
    fun post(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "post")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO is post needed?

    }

    @JavascriptInterface
    fun get(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "got")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO is get needed?

    }

    @JavascriptInterface
    fun barCodeScannerOn(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "barCodeScannerOn")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        var scannerScanJSFunction = mapParameters["scanCallback"]
        var scannerPowerButtonJSFunction = mapParameters["powerButtonCallback"]

        // TODO implement startBarcodeScanning

    }

    @JavascriptInterface
    fun findAll(parameters: String?) {

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
    fun find(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "find")

        return findAll(parameters)

    }

    @JavascriptInterface
    fun updateAll(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "updateAll")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        updateObjectsFromScriptMessage(mapParameters) then {

            javascriptCallback(it, mapParameters)

        } fail {

            javascriptCallback("$it", mapParameters)

        }

    }

    @JavascriptInterface
    fun update(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "update")

        return updateAll(parameters)

    }

    @JavascriptInterface
    fun destroy(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "destroy")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        destroyObjectFromScriptMessage(mapParameters) then {

            javascriptCallback(it, mapParameters)

        } fail {

            javascriptCallback("$it", mapParameters)

        }

    }

    @JavascriptInterface
    fun sound(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "sound")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement destroyObjectFromScriptMessage

        javascriptCallback(arrayOf("didFinishSpeaking"), null, mapParameters["callBack"] as String)

    }

    @JavascriptInterface
    fun tabbar(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "tabbar")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        return if (mapParameters["action"]?.equals("show") == true) {
            javascriptCallback(arrayOf("tabbar show success"), mapParameters)
            webViewActivity.goBack()
        } else {
            javascriptCallback(arrayOf("tabbar hide success"), mapParameters)
        }

    }

    @JavascriptInterface
    fun subscribe(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "subscribe")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        val entities = mapParameters["entities"] as? ArrayList<*>

        var errorMessage: String? = null

        val dataCallback = mapParameters["dataCallback"] as? String

        val callback = mapParameters["callback"] as? String

        if (dataCallback == null) {
            errorMessage = "No dataCallback specified"
        }

        if (callback == null) {
            errorMessage = "No callback specified"
        }

        if (entities == null) {

            errorMessage = "No entities specified"

        }

        if (errorMessage != null) {

            return javascriptCallback(errorMessage, mapParameters)

        }

        try {

            subscribeToEntities(entities!!, dataCallback!!)

        } catch (e: Exception) {

            return javascriptCallback(e.toString(), mapParameters)

        }

        javascriptCallback(arrayOf("subscribe to entities success"), mapParameters, callback!!)

    }

    @JavascriptInterface
    fun remoteControl(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "remoteControl")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement receiveRemoteCommands

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("remoteCommands ok"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun roles(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "roles")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        val callback = mapParameters["callback"]

        javascriptCallback(arrayOf(STMCoreAuthController.rolesResponse), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun checkin(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "checkin")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        val accuracy = (mapParameters["accuracy"] as? Double)?.toInt()  ?: 0

        var timeout = (mapParameters["timeout"] as? Double) ?: 30000.0

        timeout -= 2000 // otherwise web app will trigger timeout itself and bestLocation will not return

        val startTime = Date()

        var bestLocation:Location? = null

        val mFusedLocationClient = LocationServices.getFusedLocationProviderClient(MyApplication.appContext!!)

        var googleApiClient:GoogleApiClient? = null

        val locationListener = object : GoogleApiClient.ConnectionCallbacks,
                GoogleApiClient.OnConnectionFailedListener{

            @SuppressLint("MissingPermission")
            override fun onConnected(p0: Bundle?) {

                val locationRequest = LocationRequest()
                locationRequest.priority = PRIORITY_HIGH_ACCURACY
                locationRequest.interval = 0

                mFusedLocationClient.requestLocationUpdates(locationRequest, object:LocationCallback(){

                    override fun onLocationResult(p0: LocationResult?) {
                        val location = p0!!.lastLocation

                        if (bestLocation == null || bestLocation!!.accuracy > location.accuracy) {

                            bestLocation = location

                        }

                        if (bestLocation!!.accuracy <= accuracy || Date().time - timeout > startTime.time){

                            resolveLocation(bestLocation, mapParameters)

                            mFusedLocationClient.removeLocationUpdates(this)

                            googleApiClient?.disconnect()

                        }
                    }

                }, Looper.myLooper())

            }

            override fun onConnectionSuspended(p0: Int) {

                googleApiClient?.disconnect()
            }

            override fun onConnectionFailed(p0: ConnectionResult) {

                googleApiClient?.disconnect()
            }

        }

        googleApiClient = GoogleApiClient.Builder(MyApplication.appContext!!)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(locationListener)
                .addOnConnectionFailedListener(locationListener)
                .build()
        googleApiClient.connect()

    }

    fun resolveLocation(location:Location?, mapParameters:Map<*,*>){

        if (location == null){

            return javascriptCallback(MyApplication.appContext!!.resources.getString(com.sistemium.sissales.R.string.location_failed),mapParameters)

        }

        location.accuracy = Math.round(location.accuracy).toFloat()

        val data = mapParameters["data"] as Map<*, *>

        val atributes = HashMap(data)

        atributes["altitude"] = location.altitude
        atributes["horizontalAccuracy"] = location.accuracy
        atributes["latitude"] = location.latitude
        atributes["longitude"] = location.longitude
        atributes["speed"] = location.speed
        atributes["verticalAccuracy"] = location.accuracy
        atributes["isFantom"] = 0
        atributes["timestamp"] = STMFunctions.stringFrom(Date(location.time))

        persistenceDelegate.merge("STMLocation", atributes.toMap(), null).then {

            val callback = mapParameters["callback"]

            javascriptCallback(arrayOf(it), mapParameters, callback as String)

        }.fail {

            javascriptCallback(MyApplication.appContext!!.resources.getString(com.sistemium.sissales.R.string.location_failed),mapParameters)

        }

    }

    @JavascriptInterface
    fun getPicture(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "getPicture")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement getPictureWithEntityName

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf("no image data"), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun takePhoto(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "takePhoto")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        webViewActivity.photoMapParameters = mapParameters

        webViewActivity.filePath = STMCorePhotosController.sharedInstance!!.selectImage(webViewActivity)

    }

    @JavascriptInterface
    fun unsyncedInfoService(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "unsyncedInfoService")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        unsyncedInfoJSFunction = mapParameters["unsyncedInfoCallback"] as? String

    }

    @JavascriptInterface
    fun sendToCameraRoll(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "sendToCameraRoll")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement loadImageForPrimaryKey

        val callback = mapParameters["callback"]

        return javascriptCallback("", mapParameters, callback as String)

    }

    @JavascriptInterface
    fun loadImage(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "loadImage")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        val identifier = mapParameters["imageID"] as String

        val callback = mapParameters["callback"]

        STMCorePicturesController.sharedInstance?.loadImageForPrimaryKey(identifier)?.then {

            javascriptCallback(arrayListOf(it), mapParameters, callback as String)

        }?.fail {

            javascriptCallback("$it", mapParameters)

        }

    }

    @JavascriptInterface
    fun saveImage(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "saveImage")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement saveImage

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf(mapOf<Any, Any>()), mapParameters, callback as String)

    }

    @JavascriptInterface
    fun copyToClipboard(parameters: String?) {

        STMFunctions.debugLog("DEBUG", "copyToClipboard")

        val mapParameters = gson.fromJson(parameters, Map::class.java)

        // TODO implement copyToClipboard

        val callback = mapParameters["callback"]

        return javascriptCallback(arrayOf<Any>(), mapParameters, callback as String)

    }

    fun postJSNotification(notification:String){

        if (unsyncedInfoJSFunction == null) return

        javascriptCallback(arrayListOf(notification), null, unsyncedInfoJSFunction!!)

    }

    // interface handling helpers

    private fun destroyObjectFromScriptMessage(parameters: Map<*, *>): Promise<ArrayList<Map<*, *>>, Exception> {

        val entityName = parameters["entity"] as? String
                ?: throw Exception("entity is not specified")

        val xidString = parameters[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as? String

        if (xidString != null) {
            return persistenceDelegate.destroy(entityName, xidString, null).then {

                val result: Map<*, *> = hashMapOf("objectXid" to xidString)

                return@then arrayListOf(result)

            }
        } else {

            throw Exception("empty xid")

        }

    }

    private fun arrayOfObjectsRequestedByScriptMessage(parameters: Map<*, *>): Promise<ArrayList<Map<*, *>>, Exception> {

        val entityName = parameters["entity"] as? String
                ?: throw Exception("entity is not specified")

        val xidString = parameters[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as? String

        val options = parameters["options"] as? Map<*, *>

        if (xidString != null) {
            return persistenceDelegate.find(entityName, xidString, options).then {

                return@then arrayListOf(it!!)

            }
        }

        val filter = parameters["filter"] as? Map<*, *>

        val where = parameters["where"] as? Map<*, *>

        val predicate = STMPredicate.filterPredicate(filter, where, entityName)

        return persistenceDelegate.findAll(entityName, predicate, options)

    }

    private fun updateObjectsFromScriptMessage(parameters: Map<*, *>): Promise<ArrayList<Map<*, *>>, Exception> {

        val entityName = parameters["entity"] as? String
                ?: throw Exception("entity is not specified")

        var parametersData = parameters["data"] as? ArrayList<*>

        if (parametersData == null) {

            val map = parameters["data"] as? Map<*, *>

            if (map != null) {

                parametersData = arrayListOf(map)

            } else {

                throw Exception("entity is not specified")

            }

        }

        return persistenceDelegate.mergeMany(entityName, parametersData, null)

    }

    private val subscriptions = hashMapOf<String, STMWebAppInterfaceSubscription>()

    private fun subscribeToEntities(entities: ArrayList<*>, callbackName: String) {

        var subscription = subscriptions[callbackName]

        if (subscription == null) {

            subscription = STMWebAppInterfaceSubscription(callbackName)

        }

        for (entityName in entities) {

            val prfixedEntityName = STMFunctions.addPrefixToEntityName(entityName as String)

            subscription.entityNames.add(prfixedEntityName)

            updateLtsOffsetForEntityName(entityName, subscription)

        }

        for (subscriptionID in subscription.persisterSubscriptions) {

            persistenceDelegate.cancelSubscription(subscriptionID)

        }

        val persisterSubscriptions = hashSetOf<String>()

        val options = hashMapOf(STMConstants.STMPersistingOptionLts to true)

        for (entityName in subscription.entityNames) {

            persisterSubscriptions.add(persistenceDelegate.observeEntity(entityName, null, options) {

                sendSubscribedBunchOfObjects(it, entityName)

                var lts: String? = null

                for (obj in it) {

                    val objLts = (obj as? Map<*, *>)?.get(STMConstants.STMPersistingOptionLts) as? String

                    if (objLts != null) {

                        if (lts == null) {

                            lts = objLts

                        } else {

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

    private fun sendSubscribedBunchOfObjects(objectsArray: ArrayList<*>, entityName: String) {

        val matchingCallbacks = subscriptions.filter {

            return@filter it.value.entityNames.contains(entityName)

        }.keys

        if (matchingCallbacks.isEmpty()) {

            return

        }

        val _entityName = STMFunctions.removePrefixFromEntityName(entityName)

        val resultArray = objectsArray.map {

            return@map hashMapOf(
                    "entity" to _entityName,
                    "xid" to (it as Map<*, *>)["it"],
                    "data" to it
            )

        }

        for (callback in matchingCallbacks) {

            javascriptCallback(resultArray, hashMapOf("reason" to "subscription"), callback)

        }

    }

    private fun updateLtsOffsetForEntityName(entityName: String, subscription: STMWebAppInterfaceSubscription) {

        val options = hashMapOf(STMConstants.STMPersistingOptionPageSize to 1,
                STMConstants.STMPersistingOptionOrder to STMConstants.STMPersistingOptionLts,
                STMConstants.STMPersistingOptionOrderDirection to STMConstants.STMPersistingOptionOrderDirectionDescValue)

        val objects = persistenceDelegate.findAllSync(entityName, null, options)

        if (objects.firstOrNull() != null) {

            subscription.ltsOffset[entityName] = objects.first()[STMConstants.STMPersistingOptionLts] as String

        }

    }

    // callbacks

    fun javascriptCallback(data: Any, parameters: Map<*, *>?, jsCallbackFunction: String) {

        val arguments = mutableListOf<Any>()

        arguments.add(data)

        if (parameters is Map<*, *>) {

            arguments.add(parameters)

        }

        val jsFunction = "window.$jsCallbackFunction && $jsCallbackFunction.apply(null,${gson.toJson(arguments)})"

        STMFunctions.debugLog("DEBUG", "EvaluateJS")
//        STMFunctions.debugLog("JSFUNCTION", jsFunction)

        webViewActivity.webView?.post {

            webViewActivity.webView?.evaluateJavascript(jsFunction) {

                STMFunctions.debugLog("DEBUG", "Evaluate finish")

            }

        }

    }

    fun javascriptCallback(data: Any, parameters: Map<*, *>?) =
            javascriptCallback(data, parameters, this.javascriptCallback)

    fun javascriptCallback(error: String, parameters: Map<*, *>?) {

        val arguments = mutableListOf<Any>()

        arguments.add(error)

        if (parameters is Map<*, *>) {

            arguments.add(parameters)

        }

        val jsFunction = "${this.errorCallback}.apply(null, ${gson.toJson(arguments)})"

        STMFunctions.debugLog("DEBUG", "EvaluateErrorJS")
        STMFunctions.debugLog("JSFUNCTION", jsFunction)

        webViewActivity.webView?.post {

            webViewActivity.webView?.evaluateJavascript(jsFunction) {

                STMFunctions.debugLog("DEBUG", "Evaluate finish")

            }

        }

    }

}
