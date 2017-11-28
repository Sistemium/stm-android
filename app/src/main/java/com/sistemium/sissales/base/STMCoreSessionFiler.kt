package com.sistemium.sissales.base

import android.content.Context
import com.sistemium.sissales.interfaces.STMFiling
import java.io.File
import java.util.*

/**
 * Created by edgarjanvuicik on 27/11/2017.
 */
class STMCoreSessionFiler(private val context:Context): STMFiling{

    override fun bundledModelJSON(modelName: String):String {

        val assetManager = context.assets
        val stream = assetManager.open("model/$modelName.json")

        val scanner = Scanner(stream)

        val jsonModelString = StringBuilder()

        while (scanner.hasNext()) {
            jsonModelString.append(scanner.nextLine())
        }

        return jsonModelString.toString()

    }

    override fun persistencePath(folderName: String, modelName: String): String {

        return testPersistenceFile() ?: context.filesDir.absolutePath + "/" + folderName + "/" + modelName + ".db"

    }

    private fun testPersistenceFile(): String? {
        return try {

            val assetManager = context.assets
            val stream = assetManager.open("iSisSales.db")
            val file = File(context.filesDir.absolutePath+"/sqllitepath/iSisSales.db")
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeBytes(stream.readBytes())
            file.absolutePath

        }catch (e:Exception){

            null

        }
    }

}