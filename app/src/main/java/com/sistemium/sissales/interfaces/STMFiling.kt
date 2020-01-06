package com.sistemium.sissales.interfaces

import android.graphics.Bitmap

/**
 * Created by edgarjanvuicik on 27/11/2017.
 */
interface STMFiling {

    fun persistencePath(folderName: String): String
    fun saveImage(bitmap:Bitmap, folderName:String, fileName:String):String
    fun getImage(localPath:String):Bitmap?
    fun webPath(title:String): String
    fun tempWebPath(title:String): String
    fun removeOrgDirectory()

}