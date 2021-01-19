package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.classes.entitycontrollers.STMClientDataController
import com.sistemium.sissales.base.classes.entitycontrollers.STMEntityController
import com.sistemium.sissales.enums.STMSocketEvent
import com.sistemium.sissales.interfaces.STMRemoteDataEventHandling
import com.sistemium.sissales.interfaces.STMSocketConnection
import com.sistemium.sissales.interfaces.STMSocketConnectionOwner
import io.socket.client.*
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.then
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

/**
 * Created by edgarjanvuicik on 14/02/2018.
 */

class STMSocketTransport(private var socketUrlString: String, private var owner: STMSocketConnectionOwner, private var remoteDataEventHandling: STMRemoteDataEventHandling) : STMSocketConnection {

    override var isReady: Boolean = false
        get() {

            return socket?.connected() == true && isAuthorized

        }

    private var socket: Socket? = null

    private var isAuthorized = false

    init {

        startSocket()

    }

    override fun mergeAsync(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Promise<Map<*, *>, Exception> {

        val deferred = deferred<Map<*, *>, Exception>()

        val resource = STMEntityController.sharedInstance!!.resourceForEntity(entityName)

        val value = hashMapOf(
                "method" to STMConstants.kSocketUpdateMethod,
                "resource" to resource,
                "id" to attributes["id"],
                "attrs" to attributes
        )

        socketSendEvent(STMSocketEvent.STMSocketEventJSData, value)
                .then {

                    val (result, error) = respondOnData(it)

                    if (error != null) {

                        deferred.reject(error)

                    } else {

                        deferred.resolve(result!!)

                    }

                }.fail {

                    deferred.reject(it)

                }

        return deferred.promise

    }

    override fun findAllAsync(entityName: String, options: Map<*, *>?, identifier: String?): Promise<Map<*, *>, Exception> {

        val deferred = deferred<Map<*, *>, Exception>()

        val resource = STMEntityController.sharedInstance!!.resourceForEntity(entityName)

        val value = if (identifier != null) {

            hashMapOf(
                    "method" to STMConstants.kSocketFindMethod,
                    "resource" to resource,
                    "options" to options,
                    "id" to identifier
            )

        } else {

            hashMapOf(
                    "method" to STMConstants.kSocketFindAllMethod,
                    "resource" to resource,
                    "options" to options
            )

        }

        socketSendEvent(STMSocketEvent.STMSocketEventJSData, value)
                .then {

                    val (result, error) = respondOnData(it)

                    if (error != null) {

                        deferred.reject(error)

                    } else {

                        deferred.resolve(result!!)

                    }

                }.fail {

                    STMFunctions.debugLog("",entityName)

                    deferred.reject(it)

                }

        return deferred.promise

    }

    override fun socketSendEvent(event: STMSocketEvent, value: Any?): Promise<Array<*>, Exception> {

        val deferred = deferred<Array<*>, Exception>()

        val _value: Any? = if (value is HashMap<*, *>) JSONObject(value) else if (value is ArrayList<*>) JSONArray(value) else value as? JSONObject

        if (!isReady) {

            val errorMessage = "socket not connected while sendEvent"
            socketLostConnection(errorMessage)

            deferred.reject(Exception(errorMessage))

            return deferred.promise

        }

        if (event == STMSocketEvent.STMSocketEventJSData) {

            if (_value !is JSONObject) {

                deferred.reject(Exception("STMSocketEventJSData value is not JSONObject"))

                return deferred.promise

            }

            socket!!.emit(event.toString(), _value, object: AckWithTimeOut(STMConstants.AUTH_DELAY.toLong() * 1000){

                override fun call(vararg args: Any?) {

                    if (args.firstOrNull() is NoAck) {

                        deferred.reject(Exception("ack timeout on event $event"))

                        return

                    }

                    deferred.resolve(args)

                }

            })

            return deferred.promise

        }

        if (event == STMSocketEvent.STMSocketEventInfo){

            socket!!.emit(event.toString(), value, object: AckWithTimeOut(STMConstants.AUTH_DELAY.toLong() * 1000){

                override fun call(vararg args: Any?) {

                    if (args.firstOrNull() is NoAck) {

                        deferred.reject(Exception("ack timeout on event $event"))

                        return

                    }

                    deferred.resolve(args)

                }

            })

            return deferred.promise

        }

        val primaryKey = primaryKeyForEvent(event)

        if (primaryKey != null && _value != null) {

            val dataDic = hashMapOf(primaryKey to _value)
            val message = "send via socket for event: $event"
            STMFunctions.debugLog("STMSocketTransport", message)
            deferred.resolve(arrayOf(dataDic))

        } else if (_value != null) {
            socket!!.emit(event.toString(), value, object: AckWithTimeOut(STMConstants.AUTH_DELAY.toLong() * 1000){

                override fun call(vararg args: Any?) {

                    if (args.firstOrNull() is NoAck) {

                        deferred.reject(Exception("ack timeout on event $event"))

                        return

                    }

                    deferred.resolve(args)

                }

            })
        } else {

            socket!!.emit(event.toString(), value, object: AckWithTimeOut(STMConstants.AUTH_DELAY.toLong() * 1000){

                override fun call(vararg args: Any?) {

                    if (args.firstOrNull() is NoAck) {

                        deferred.reject(Exception("ack timeout on event $event with null value"))

                        return

                    }

                    deferred.resolve(args)

                }

            })

        }

        return deferred.promise

    }

    override fun closeSocket() {

        STMLogger.sharedLogger!!.infoMessage("close Socket")
        socket?.off()
        socket?.disconnect()
        owner.socketWillClosed()
        socket = null
        isAuthorized = false

    }

    private fun respondOnData(array: Array<*>): Pair<Map<*, *>?, Exception?> {

        if (array.size != 1) {

            return Pair(null, Exception("Response length is not 1"))

        }

        val stResponse = array.firstOrNull() as? JSONObject
                ?: return Pair(null, Exception("No stResponse "))

        if (stResponse.has("error")) {

            return Pair(null, java.lang.Exception(stResponse["error"].toString()))

        }

        return Pair(STMFunctions.gson.fromJson(stResponse.toString(), Map::class.java), null)

    }

    fun startSocket() {

        STMLogger.sharedLogger!!.infoMessage("STMSocketTransport")

        val o = IO.Options()

        val u = URI(socketUrlString.replace("//socket.", "//socket-v2.")) //production
//        val u = URI("http://10.0.1.5:8000/socket.io-client") //work
//        val u = URI("http://192.168.0.105:8000/socket.io-client") //home

        o.path = u.path + "/"

        socket = IO.socket(u.toString().removeSuffix(u.path), o)

        addEventObservers()

        socket!!.connect()

    }

    private fun addEventObservers() {

        socket?.off()

        STMLogger.sharedLogger!!.infoMessage("addEventObserversToSocket")

        socket!!.on(STMSocketEvent.STMSocketEventConnect.toString()) {

            STMFunctions.debugLog("SOCKET", "STMSocketEventConnect")

            emitAuthorization()

        }

        socket!!.on(STMSocketEvent.STMSocketEventDisconnect.toString()) {

            STMFunctions.debugLog("SOCKET", "STMSocketEventDisconnect")

            owner.socketWillClosed()

        }

        socket!!.on(STMSocketEvent.STMSocketEventError.toString()) {

            STMFunctions.debugLog("SOCKET", "STMSocketEventError")

            reconnectSocket()

        }

        socket!!.on(STMSocketEvent.STMSocketEventReconnect.toString()) {

            STMFunctions.debugLog("SOCKET", "STMSocketEventReconnect")

            reconnectSocket()

        }

        socket!!.on(STMSocketEvent.STMSocketEventRemoteCommands.toString()) {

            STMFunctions.debugLog("STMSocketTransport","got Remote Commands")

        }

        socket!!.on(STMSocketEvent.STMSocketEventRemoteRequests.toString()) {

            STMFunctions.debugLog("STMSocketTransport","got Remote Request")

        }

        socket!!.on(STMSocketEvent.STMSocketEventUpdate.toString()) {

            STMFunctions.debugLog("SOCKET", "STMSocketEventUpdate")

            updateEventHandleWithData(it)

        }

        socket!!.on(STMSocketEvent.STMSocketEventUpdateCollection.toString()) {

            STMFunctions.debugLog("SOCKET", "STMSocketEventUpdateCollection")

            updateEventHandleWithData(it)

        }

        socket!!.on(STMSocketEvent.STMSocketEventDestroy.toString()) {

            STMFunctions.debugLog("SOCKET", "STMSocketEventDestroy")

            destroyEventHandleWithData(it)

        }

        socket!!.on(Socket.EVENT_CONNECT_ERROR){

            STMFunctions.debugLog("SOCKET", "EVENT_CONNECT_ERROR")

            owner.socketWillClosed()

        }

        socket!!.on(Socket.EVENT_CONNECT_TIMEOUT){

            STMFunctions.debugLog("SOCKET", "EVENT_CONNECT_TIMEOUT")

        }

        socket!!.on(Socket.EVENT_DISCONNECT){

            STMFunctions.debugLog("SOCKET", "EVENT_DISCONNECT")

        }

        socket!!.on(Socket.EVENT_CONNECTING){

            STMFunctions.debugLog("SOCKET", "EVENT_CONNECTING")

        }

        socket!!.on(Socket.EVENT_ERROR){

            STMFunctions.debugLog("SOCKET", "EVENT_ERROR")

        }

        socket!!.on(Socket.EVENT_MESSAGE){

            STMFunctions.debugLog("SOCKET", "EVENT_MESSAGE")

        }

        socket!!.on(Socket.EVENT_PING){

//            STMFunctions.debugLog("SOCKET", "EVENT_PING")

        }

        socket!!.on(Socket.EVENT_PONG){

//            STMFunctions.debugLog("SOCKET", "EVENT_PONG")

        }

        socket!!.on(Socket.EVENT_RECONNECT){

            STMFunctions.debugLog("SOCKET", "EVENT_RECONNECT")

        }

        socket!!.on(Socket.EVENT_RECONNECTING){

            STMFunctions.debugLog("SOCKET", "EVENT_RECONNECTING")

        }

        socket!!.on(Socket.EVENT_RECONNECT_ATTEMPT){

            STMFunctions.debugLog("SOCKET", "EVENT_RECONNECT_ATTEMPT ${it.first()}")

            if ((it.first() as Int) % 10 == 0){

//                STMLogger.sharedLogger?.importantMessage("EVENT_RECONNECT_ATTEMPT ${it.first()}")

            }

        }

        socket!!.on(Socket.EVENT_RECONNECT_ERROR){

            STMFunctions.debugLog("SOCKET", "EVENT_RECONNECT_ERROR")

        }

        socket!!.on(Socket.EVENT_RECONNECT_FAILED){

            STMFunctions.debugLog("SOCKET", "EVENT_RECONNECT_FAILED")

        }

    }

    override fun reconnectSocket() {

        closeSocket()
        startSocket()

    }

    private fun emitAuthorization() {

        var dataDic = STMClientDataController.clientData

        val authDic = hashMapOf(
                "userId" to STMCoreAuthController.userID,
                "accessToken" to STMCoreAuthController.accessToken,
                "deviceUUID" to STMFunctions.deviceUUID(),
                "bundleIdentifier" to STMCoreAuthController.userAgent.split("/").first(),
                "appVersion" to STMCoreAuthController.userAgent.split("/").last()
        )

        dataDic += authDic

        val logMessage = "send authorization data $dataDic"

        STMLogger.sharedLogger!!.infoMessage(logMessage)

        val eventNum = STMSocketEvent.STMSocketEventAuthorization

        val event = eventNum.toString()

        socket!!.emit(event, JSONObject(dataDic), object: AckWithTimeOut(STMConstants.AUTH_DELAY.toLong() * 1000){

            override fun call(vararg args: Any?) {
                receiveAuthorizationAckWithData(args)
            }

        })

    }

    private fun receiveAuthorizationAckWithData(data: Array<*>) {

        if (data.firstOrNull() is NoAck) {

            notAuthorizedWithError("receiveAuthorizationAckWithData authorization timeout")

        }

        var logMessage = "receiveAuthorizationAckWithData ${data.firstOrNull()}"

        STMLogger.sharedLogger!!.infoMessage(logMessage)

        val dataDic = data.firstOrNull() as? JSONObject
                ?: return notAuthorizedWithError("socket receiveAuthorizationAck with data.firstOrNull() is not a JSONObject")

        if (!dataDic.has("isAuthorized")) {

            STMFunctions.debugLog("STMSocketTransport", dataDic.toString())

            delayedAuthorization()

        }

        isAuthorized = dataDic.getBoolean("isAuthorized")

        if (!isAuthorized) {

            notAuthorizedWithError("socket receiveAuthorizationAck with dataDic.isAuthorized == false")

        }

        logMessage = "socket authorized"

        STMLogger.sharedLogger!!.infoMessage(logMessage)

        owner.socketReceiveAuthorization()

        checkAppState()

    }

    private fun notAuthorizedWithError(errorString: String) {

        STMLogger.sharedLogger?.importantMessage(errorString)

    }

    private fun delayedAuthorization() {

        android.os.Handler(MyApplication.appContext!!.mainLooper).postDelayed({

            emitAuthorization()

        }, STMConstants.AUTH_DELAY.toLong() * 1000)

    }

    private fun checkAppState() {

        val appState = if (MyApplication.inBackground) "UIApplicationStateBackground" else "UIApplicationStateActive"

        socketSendEvent(STMSocketEvent.STMSocketEventStatusChange, appState)

    }

    private fun socketLostConnection(infoString: String) {

        STMLogger.sharedLogger!!.infoMessage("Socket lost connection: $infoString")

        owner.socketWillClosed()

    }

    private fun primaryKeyForEvent(event: STMSocketEvent): String? {

        if (event == STMSocketEvent.STMSocketEventSubscribe) {

            return null

        }

        if (event == STMSocketEvent.STMSocketEventData) {

            return "data"

        }

        return "url"

    }

    private fun updateEventHandleWithData(data: Array<*>) {

        STMFunctions.debugLog("STMSocketTransport", "updateEventHandleWithData")

        val receivedData = data.firstOrNull() as? JSONObject

        if (receivedData?.get("resource") != null) {

            val entityName = (receivedData["resource"] as String).split("/").last()

            val d = receivedData["data"] as? JSONObject

            if (d != null && d.has("id")) {

                remoteDataEventHandling.remoteUpdated(entityName, STMFunctions.gson.fromJson(d.toString(), Map::class.java))

            } else {

                remoteDataEventHandling.remoteHasNewData(entityName)

            }

        }

    }

    private fun destroyEventHandleWithData(data: Array<*>) {

        STMFunctions.debugLog("STMSocketTransport", "destroyEventHandleWithData")

        val receivedData = data.firstOrNull() as? JSONObject

        if (receivedData?.get("resource") != null) {

            val entityName = (receivedData["resource"] as String).split("/").last()

            val d = receivedData["data"] as? JSONObject?

            if (d != null && d.has("id")) {

                remoteDataEventHandling.remoteDestroyed(entityName, d["id"] as String)

            }

        }

    }

}