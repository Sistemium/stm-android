package com.sistemium.sissales.webInterface

/**
 * Created by edgarjanvuicik on 25/01/2018.
 */
class STMWebAppInterfaceSubscription(val callbackName:String) {

    val entityNames = hashSetOf<String>()
    val ltsOffset = hashMapOf<String,String>()
    var persisterSubscriptions = hashSetOf<String>()

}