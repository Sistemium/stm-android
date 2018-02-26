package com.sistemium.sissales.base.session

import android.util.Log
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.calsses.entitycontrollers.STMClientDataController
import com.sistemium.sissales.enums.STMSocketEvent
import com.sistemium.sissales.interfaces.STMRemoteDataEventHandling
import com.sistemium.sissales.interfaces.STMSocketConnection
import com.sistemium.sissales.interfaces.STMSocketConnectionOwner
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import nl.komponents.kovenant.Deferred
import nl.komponents.kovenant.deferred
import org.json.JSONObject
import java.net.URI

/**
 * Created by edgarjanvuicik on 14/02/2018.
 */

class STMSocketTransport(var socketUrlString:String, var entityResource:String, var owner:STMSocketConnectionOwner, var remoteDataEventHandling: STMRemoteDataEventHandling):STMSocketConnection {

    private var socket:Socket? = null

    var isAuthorized = false

    override var isReady:Boolean = false
    get() {

        return socket?.connected() == true && isAuthorized

    }

    init {

        startSocket()

    }

    fun startSocket(){

        STMLogger.sharedLogger.infoMessage("STMSocketTransport")

        val o = IO.Options()

//        val u = URI(socketUrlString)

        val u = URI("http://10.0.1.5:8000/socket.io-client")

        o.path = u.path + "/"

        socket = IO.socket(u.toString().removeSuffix(u.path), o)

        addEventObservers()

        socket!!.connect()

    }

    fun addEventObservers(){

        socket?.off()

        STMLogger.sharedLogger.infoMessage("addEventObserversToSocket")

        socket!!.on(STMSocketEvent.STMSocketEventConnect.toString()){

            emitAuthorization()

        }

        socket!!.on(STMSocketEvent.STMSocketEventDisconnect.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventError.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventReconnect.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventReconnectAttempt.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventRemoteCommands.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventRemoteRequests.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventData.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventJSData.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventUpdate.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventUpdateCollection.toString()){

            Log.d(",","")

        }

        socket!!.on(STMSocketEvent.STMSocketEventDestroy.toString()){

            Log.d(",","")

        }

    }

    fun emitAuthorization(){

        var dataDic = STMClientDataController.clientData

        val authDic = hashMapOf(
                "userId" to STMCoreAuthController.userID,
                "accessToken" to STMCoreAuthController.accessToken
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

    fun receiveAuthorizationAckWithData(data:Array<*>){

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

    fun notAuthorizedWithError(errorString:String){

        TODO("not implemented")

    }

    fun delayedAuthorization(){

        android.os.Handler(MyApplication.appContext!!.mainLooper).postDelayed({

            emitAuthorization()

        }, STMConstants.AUTH_DELAY.toLong())

    }

    fun checkAppState(){

        val appState = if (MyApplication.inBackground) "UIApplicationStateBackground" else "UIApplicationStateActive"

        socketSendEvent(STMSocketEvent.STMSocketEventStatusChange, appState)

    }

    override fun socketSendEvent(event: STMSocketEvent, value: Any?): Deferred<Pair<Boolean, Array<*>?>, Exception> {

        val deferred = deferred<Pair<Boolean, Array<*>?>,Exception>()

        if (!isReady){

            val errorMessage = "socket not connected while sendEvent"
            socketLostConnection(errorMessage)

            deferred.resolve(Pair(false, null))

            return deferred

        }

        if (event == STMSocketEvent.STMSocketEventJSData){

            if (value !is JSONObject){

                deferred.reject(Exception("STMSocketEventJSData value is not JSONObject"))

                return deferred

            }

            socket!!.emit(event.toString(), value, Ack{

                if (it.firstOrNull() == "NO ACK"){

                    deferred.reject(Exception("ack timeout"))

                }

                deferred.resolve(Pair(true, it))

            })

            return deferred

        }

        val primaryKey = primaryKeyForEvent(event)

        if (primaryKey != null && value != null) {

            val dataDic = hashMapOf(primaryKey to value)
            val message = "send via socket for event: $event"
            STMFunctions.debugLog("STMSocketTransport", message)
            deferred.resolve(Pair(true, arrayOf(dataDic)))

        } else if (value != null) {
            socket!!.emit(event.toString(), arrayOf(value), {

                if (it.firstOrNull() == "NO ACK"){

                    deferred.reject(Exception("ack timeout"))

                }

                deferred.resolve(Pair(true, it))

            })
        } else {

            socket!!.emit(event.toString(), Ack {

                if (it.firstOrNull() == "NO ACK"){

                    deferred.reject(Exception("ack timeout"))

                }

                deferred.resolve(Pair(true, it))

            })

        }

        return deferred

    }

    fun socketLostConnection(infoString:String){

        TODO("not implemented")

    }

    fun primaryKeyForEvent(event: STMSocketEvent):String?{

        if (event == STMSocketEvent.STMSocketEventSubscribe){

            return null

        }

        if (event == STMSocketEvent.STMSocketEventData){

            return "data"

        }

        return "url"

    }

}