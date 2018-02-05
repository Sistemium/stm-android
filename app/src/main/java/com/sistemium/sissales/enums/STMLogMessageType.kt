package com.sistemium.sissales.enums

/**
 * Created by edgarjanvuicik on 31/01/2018.
 */
enum class STMLogMessageType(private val type:String) {

    STMLogMessageTypeImportant("important"),
    STMLogMessageTypeError("error"),
    STMLogMessageTypeWarning("warning"),
    STMLogMessageTypeInfo("info"),
    STMLogMessageTypeDebug("debug");

    override fun toString():String{

        return type

    }

}