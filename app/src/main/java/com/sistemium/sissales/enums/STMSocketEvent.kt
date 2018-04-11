package com.sistemium.sissales.enums

/**
 * Created by edgarjanvuicik on 15/02/2018.
 */
enum class STMSocketEvent(private val type: String) {

    STMSocketEventConnect("connect"),
    STMSocketEventDisconnect("disconnect"),
    STMSocketEventError("error"),
    STMSocketEventReconnect("reconnect"),
    STMSocketEventReconnectAttempt("reconnectAttempt"),
    STMSocketEventStatusChange("status:change"),
    STMSocketEventInfo("info"),
    STMSocketEventAuthorization("authorization"),
    STMSocketEventRemoteCommands("remoteCommands"),
    STMSocketEventRemoteRequests("remoteRequests"),
    STMSocketEventData("data:v1"),
    STMSocketEventJSData("jsData"),
    STMSocketEventSubscribe("jsData:subscribe"),
    STMSocketEventUpdate("jsData:update"),
    STMSocketEventUpdateCollection("jsData:updateCollection"),
    STMSocketEventDestroy("jsData:destroy");

    override fun toString(): String {

        return type

    }

}