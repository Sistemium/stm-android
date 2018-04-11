package com.sistemium.sissales.base

import com.sistemium.sissales.base.STMConstants.Companion.SHARED_PATH
import com.sistemium.sissales.interfaces.STMDirectoring
import com.sistemium.sissales.interfaces.STMFiling
import java.io.File
import java.util.*

/**
 * Created by edgarjanvuicik on 27/11/2017.
 */
class STMCoreSessionFiler(org: String, uid: String) : STMFiling, STMDirectoring {

    private val userDocuments = MyApplication.appContext!!.filesDir.absolutePath + "/" + org + "/" + uid
    private val sharedDocuments = MyApplication.appContext!!.filesDir.absolutePath + "/" + org + "/" + SHARED_PATH
    private val directoring: STMDirectoring = this
    private val persistenceBasePath = (testPersistencePath()
            ?: userDocuments) + "/" + STMConstants.PERSISTENCE_PATH

    override fun bundledModelJSON(modelName: String): String {

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

        val path = persistenceBasePath + "/" + folderName

        File(path).mkdirs()

        return path

    }

    private fun testPersistencePath(): String? {
        return try {

            val assetManager = MyApplication.appContext!!.assets
            var stream = assetManager.open("model/iSisSales.json")
            var file = File(MyApplication.appContext!!.filesDir.absolutePath + "/testPersistenceFile/${STMConstants.PERSISTENCE_PATH}/${STMConstants.SQL_LITE_PATH}/iSisSales.json")
            file.parentFile.mkdirs()
            file.createNewFile()
            file.writeBytes(stream.readBytes())

            stream = assetManager.open("iSisSales.db")
            file = File(MyApplication.appContext!!.filesDir.absolutePath + "/testPersistenceFile/${STMConstants.PERSISTENCE_PATH}/${STMConstants.SQL_LITE_PATH}/iSisSales.db")

            if (!file.exists()) {

                file.parentFile.mkdirs()
                file.createNewFile()
                file.writeBytes(stream.readBytes())

            }

            MyApplication.appContext!!.filesDir.absolutePath + "/testPersistenceFile"

        } catch (e: Exception) {

            null

        }
    }

}