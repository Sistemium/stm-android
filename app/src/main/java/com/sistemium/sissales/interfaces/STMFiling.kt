package com.sistemium.sissales.interfaces

/**
 * Created by edgarjanvuicik on 27/11/2017.
 */
interface STMFiling {

    fun persistencePath(folderName:String):String
    fun bundledModelJSON(modelName:String):String

}