package com.sistemium.sissales.base.session

import android.util.Log
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.enums.STMSocketEvent
import com.sistemium.sissales.interfaces.STMRemoteDataEventHandling
import com.sistemium.sissales.interfaces.STMSocketConnection
import com.sistemium.sissales.interfaces.STMSocketConnectionOwner
import io.socket.client.IO
import io.socket.client.Socket

/**
 * Created by edgarjanvuicik on 14/02/2018.
 */

class STMSocketTransport(var socketUrlString:String, var entityResource:String, var owner:STMSocketConnectionOwner, var remoteDataEventHandling: STMRemoteDataEventHandling):STMSocketConnection {

    private var socket:Socket? = null

    init {

        startSocket()

    }

    fun startSocket(){

        STMLogger.sharedLogger.infoMessage("STMSocketTransport")

//        socket = IO.socket(socketUrlString)

        val o = IO.Options()

        o.path = "/socket.io-client"

        socket = IO.socket("http://10.0.1.5:8000/socket.io-client", o)

        addEventObservers()

//        socket!!.on(Socket.EVENT_CONNECT){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_ERROR){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_CONNECTING){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_CONNECT_ERROR){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_CONNECT_TIMEOUT){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_DISCONNECT){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_MESSAGE){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_PING){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_PONG){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_RECONNECT){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_RECONNECTING){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_RECONNECT_ATTEMPT){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_RECONNECT_ERROR){
//
//            Log.d("j","j")
//
//        }
//
//        socket!!.on(Socket.EVENT_RECONNECT_FAILED){
//
//            Log.d("j","j")
//
//        }

        socket!!.connect()

    }

    fun addEventObservers(){

        socket?.off()

        STMLogger.sharedLogger.infoMessage("addEventObserversToSocket")

        val events = arrayListOf(
                STMSocketEvent.STMSocketEventConnect,
                STMSocketEvent.STMSocketEventDisconnect,
                STMSocketEvent.STMSocketEventError,
                STMSocketEvent.STMSocketEventReconnect,
                STMSocketEvent.STMSocketEventReconnectAttempt,
                STMSocketEvent.STMSocketEventRemoteCommands,
                STMSocketEvent.STMSocketEventRemoteRequests,
                STMSocketEvent.STMSocketEventData,
                STMSocketEvent.STMSocketEventJSData,
                STMSocketEvent.STMSocketEventUpdate,
                STMSocketEvent.STMSocketEventUpdateCollection,
                STMSocketEvent.STMSocketEventDestroy
        )

        for (event in events){

            addHandlerForEvent(event)

        }

    }

    fun addHandlerForEvent(event:STMSocketEvent){

        val emit = socket!!.on(event.toString()){

            Log.d("","")

        }

    }

}