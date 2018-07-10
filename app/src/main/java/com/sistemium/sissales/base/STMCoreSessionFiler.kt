package com.sistemium.sissales.base

import android.content.Context
import com.sistemium.sissales.base.STMConstants.Companion.SHARED_PATH
import com.sistemium.sissales.interfaces.STMDirectoring
import com.sistemium.sissales.interfaces.STMFiling
import java.io.File
import java.util.*

/**
 * Created by edgarjanvuicik on 27/11/2017.
 */
class STMCoreSessionFiler(org: String, uid: String, var context:Context? = MyApplication.appContext) : STMFiling, STMDirectoring {

    private val userOrg = context!!.filesDir.absolutePath + "/" + org
    private val userDocuments = "$userOrg/$uid"
    private val persistenceBasePath = userDocuments + "/" + STMConstants.PERSISTENCE_PATH

    override fun bundledModelJSON(modelName: String): String {

        val assetManager = context!!.assets
        val stream = assetManager.open("model/$modelName.json")

        val scanner = Scanner(stream)

        val jsonModelString = StringBuilder()

        while (scanner.hasNext()) {
            jsonModelString.append(scanner.nextLine())
        }

        return jsonModelString.toString()

    }

    override fun persistencePath(folderName: String): String {

        val path = "$persistenceBasePath/$folderName"

        File(path).mkdirs()

        return path

    }

    override fun removeOrgDirectory() {

        STMFunctions.deleteRecursive(File(userOrg))

    }

}