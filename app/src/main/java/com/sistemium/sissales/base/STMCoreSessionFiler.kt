package com.sistemium.sissales.base

import com.sistemium.sissales.base.STMConstants.Companion.SHARED_PATH
import com.sistemium.sissales.interfaces.STMDirectoring
import com.sistemium.sissales.interfaces.STMFiling
import java.io.File
import java.util.*

/**
 * Created by edgarjanvuicik on 27/11/2017.
 */
class STMCoreSessionFiler(org:String, uid:String): STMFiling, STMDirectoring {

    val userDocuments = MyApplication.appContext!!.filesDir.absolutePath + "/" + org + "/" + uid
    val sharedDocuments = MyApplication.appContext!!.filesDir.absolutePath + "/" + org + "/" + SHARED_PATH
    val directoring:STMDirectoring = this
    val persistenceBasePath = (testPersistencePath() ?: userDocuments) + "/" + STMConstants.PERSISTENCE_PATH

    override fun bundledModelJSON(modelName: String):String {

        val assetManager = MyApplication.appContext!!.assets
        val stream = assetManager.open("model/$modelName.json")

        val scanner = Scanner(stream)

        val jsonModelString = StringBuilder()

        while (scanner.hasNext()) {
            jsonModelString.append(scanner.nextLine())
        }

        return jsonModelString.toString()

    }

    override fun persistencePath(folderName: String): String {

        return persistenceBasePath + "/" + folderName

    }

    private fun testPersistencePath(): String? {
        return try {

            val assetManager = MyApplication.appContext!!.assets
            var stream = assetManager.open("model/iSisSales.json")
            var file = File(MyApplication.appContext!!.filesDir.absolutePath+"/testPersistenceFile/${STMConstants.PERSISTENCE_PATH}/${STMConstants.SQL_LITE_PATH}/iSisSales.json")
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeBytes(stream.readBytes())

            stream = assetManager.open("iSisSales.db")
            file = File(MyApplication.appContext!!.filesDir.absolutePath+"/testPersistenceFile/${STMConstants.PERSISTENCE_PATH}/${STMConstants.SQL_LITE_PATH}/iSisSales.db")
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeBytes(stream.readBytes())
            MyApplication.appContext!!.filesDir.absolutePath+"/testPersistenceFile"

        }catch (e:Exception){

            null

        }
    }

}