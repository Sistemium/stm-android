package com.sistemium.sissales.base.session

import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.calsses.entitycontrollers.STMClientDataController
import com.sistemium.sissales.calsses.entitycontrollers.STMEntityController
import com.sistemium.sissales.enums.STMSocketEvent
import com.sistemium.sissales.interfaces.STMRemoteDataEventHandling
import com.sistemium.sissales.interfaces.STMSocketConnection
import com.sistemium.sissales.interfaces.STMSocketConnectionOwner
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.then
import org.json.JSONArray
import org.json.JSONObject
import java.net.URI

/**
 * Created by edgarjanvuicik on 14/02/2018.
 */

class STMSocketTransport(var socketUrlString:String, var entityResource:String, var owner:STMSocketConnectionOwner, var remoteDataEventHandling: STMRemoteDataEventHandling):STMSocketConnection {

    override var isReady:Boolean = false
        get() {

            return socket?.connected() == true && isAuthorized

        }

    private var socket:Socket? = null

    private var isAuthorized = false

    init {

        startSocket()

    }

    override fun mergeAsync(entityName: String, attributes: Map<*, *>, options: Map<*, *>?): Promise<Map<*, *>, Exception> {

        val deferred = deferred<Map<*, *>, Exception>()

        val resource = STMEntityController.sharedInstance.resourceForEntity(entityName)

        val value = hashMapOf(
                "method" to STMConstants.kSocketUpdateMethod,
                "resource" to resource,
                "id" to attributes["id"],
                "attrs" to attributes
        )

        socketSendEvent(STMSocketEvent.STMSocketEventJSData, value)
                .then {

                        val (result, error) =  respondOnData(it)

                        if (error != null){

                            deferred.reject(error)

                        }else{

                            deferred.resolve(result!!)

                        }

                }.fail {

                    deferred.reject(it)

                }

        return deferred.promise

    }

    override fun findAllAsync(entityName: String, options: Map<*, *>?): Promise<Map<*, *>, Exception> {

        val deferred = deferred<Map<*,*>, Exception>()

        val errorMessage = preFindAllAsyncCheckForEntityName(entityName)

        if (errorMessage != null) {

            deferred.reject(Exception(errorMessage))

        }

        val resource = STMEntityController.sharedInstance.resourceForEntity(entityName)

        val value = hashMapOf(
                "method" to STMConstants.kSocketFindAllMethod,
                "resource" to resource,
                "options" to options
        )

        socketSendEvent(STMSocketEvent.STMSocketEventJSData, value)
                .then {

                    val (result, error) =  respondOnData(it)

                    if (error != null){

                        deferred.reject(error)

                    }else{

                        deferred.resolve(result!!)

                    }

                }.fail {

                    deferred.reject(it)

                }

        return deferred.promise

    }

    override fun socketSendEvent(event: STMSocketEvent, value: Any?): Promise<Array<*>, Exception> {

        val deferred = deferred<Array<*>,Exception>()

        val _value:Any? = if (value is HashMap<*, *>) JSONObject(value) else if (value is ArrayList<*>) JSONArray(value) else value as? JSONObject

        if (!isReady){

            val errorMessage = "socket not connected while sendEvent"
            socketLostConnection(errorMessage)

            deferred.reject(Exception(errorMessage))

            return deferred.promise

        }

        if (event == STMSocketEvent.STMSocketEventJSData){

            if (_value !is JSONObject){

                deferred.reject(Exception("STMSocketEventJSData value is not JSONObject"))

                return deferred.promise

            }

            socket!!.emit(event.toString(), _value, Ack{

                if (it.firstOrNull() == "NO ACK"){

                    deferred.reject(Exception("ack timeout"))

                }

                deferred.resolve(it)

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
            socket!!.emit(event.toString(), arrayOf(_value), {

                if (it.firstOrNull() == "NO ACK"){

                    deferred.reject(Exception("ack timeout"))

                }

                deferred.resolve(it)

            })
        } else {

            socket!!.emit(event.toString(), Ack {

                if (it.firstOrNull() == "NO ACK"){

                    deferred.reject(Exception("ack timeout"))

                }

                deferred.resolve(it)

            })

        }

        return deferred.promise

    }

    private fun respondOnData(array: Array<*>): Pair<Map<*, *>?, Exception?>{

        if (array.size != 1){

            return Pair(null, Exception("Response length is not 1"))

        }

        val stResponse = array.firstOrNull() as? JSONObject ?: return Pair(null, Exception("No stResponse "))

        return Pair(STMFunctions.gson.fromJson(stResponse.toString(), Map::class.java), null)

    }

    private fun startSocket(){

        STMLogger.sharedLogger.infoMessage("STMSocketTransport")

        val o = IO.Options()

//        val u = URI(socketUrlString) //production
        val u = URI("http://10.0.1.5:8000/socket.io-client") //work
//        val u = URI("http://192.168.0.106:8000/socket.io-client") //home

        o.path = u.path + "/"

        socket = IO.socket(u.toString().removeSuffix(u.path), o)

        addEventObservers()

        socket!!.connect()

    }

    private fun addEventObservers(){

        socket?.off()

        STMLogger.sharedLogger.infoMessage("addEventObserversToSocket")

        socket!!.on(STMSocketEvent.STMSocketEventConnect.toString()){

            emitAuthorization()

        }

        socket!!.on(STMSocketEvent.STMSocketEventDisconnect.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventError.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventReconnect.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventReconnectAttempt.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventRemoteCommands.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventRemoteRequests.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventData.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventJSData.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventUpdate.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventUpdateCollection.toString()){

            TODO("not implemented")

        }

        socket!!.on(STMSocketEvent.STMSocketEventDestroy.toString()){

            TODO("not implemented")

        }

    }

    private fun emitAuthorization(){

        //TODO clientData is empty
        var dataDic = STMClientDataController.clientData

        val authDic = hashMapOf(
                "userId" to STMCoreAuthController.userID,
                "accessToken" to STMCoreAuthController.accessToken,
                "deviceUUID" to STMFunctions.deviceUUID(),
                "bundleIdentifier" to STMConstants.userAgent.split("/").first(),
                "appVersion" to STMConstants.userAgent.split("/").last()
        )

        dataDic += authDic

        val logMessage = "send authorization data $dataDic"

        STMLogger.sharedLogger.infoMessage(logMessage)

        val eventNum = STMSocketEvent.STMSocketEventAuthorization

        val event = eventNum.toString()

        socket!!.emit(event, JSONObject(dataDic), Ack{

            receiveAuthorizationAckWithData(it)

        })

    }

    private fun receiveAuthorizationAckWithData(data:Array<*>){

        if (data.firstOrNull() == "NO ACK"){

            notAuthorizedWithError("receiveAuthorizationAckWithData authorization timeout")

        }

        var logMessage = "receiveAuthorizationAckWithData ${data.firstOrNull()}"

        STMLogger.sharedLogger.infoMessage(logMessage)

        val dataDic = data.firstOrNull() as? JSONObject ?: return notAuthorizedWithError("socket receiveAuthorizationAck with data.firstOrNull() is not a JSONObject")

        if (!dataDic.has("isAuthorized")){

            STMFunctions.debugLog("STMSocketTransport", dataDic.toString())

            delayedAuthorization()

        }

        isAuthorized = dataDic.getBoolean("isAuthorized")

        if (!isAuthorized){

            notAuthorizedWithError("socket receiveAuthorizationAck with dataDic.isAuthorized == false")

        }

        logMessage = "socket authorized"

        STMLogger.sharedLogger.infoMessage(logMessage)

        owner.socketReceiveAuthorization()

        checkAppState()

    }

    private fun notAuthorizedWithError(errorString:String){

        TODO("not implemented")

    }

    private fun delayedAuthorization(){

        android.os.Handler(MyApplication.appContext!!.mainLooper).postDelayed({

            emitAuthorization()

        }, STMConstants.AUTH_DELAY.toLong())

    }

    private fun checkAppState(){

        val appState = if (MyApplication.inBackground) "UIApplicationStateBackground" else "UIApplicationStateActive"

        socketSendEvent(STMSocketEvent.STMSocketEventStatusChange, appState)

    }

    private fun socketLostConnection(infoString:String){

        TODO("not implemented")

    }

    private fun primaryKeyForEvent(event: STMSocketEvent):String?{

        if (event == STMSocketEvent.STMSocketEventSubscribe){

            return null

        }

        if (event == STMSocketEvent.STMSocketEventData){

            return "data"

        }

        return "url"

    }

    private fun preFindAllAsyncCheckForEntityName(entityName:String):String?{

        if (!isReady){
            return "socket is not ready (not connected or not authorized)"
        }
        val entity = STMEntityController.sharedInstance.stcEntities?.get(entityName) ?: return "have no such entity $entityName"

        val resource = STMEntityController.sharedInstance.resourceForEntity(entityName)

        return null

    }

}