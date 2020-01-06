package com.sistemium.sissales.base

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.os.Environment
import android.provider.MediaStore
import com.sistemium.sissales.interfaces.STMFiling
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*
import android.provider.MediaStore.Images.Media.getBitmap
import android.app.Activity
import android.graphics.BitmapFactory


/**
 * Created by edgarjanvuicik on 27/11/2017.
 */
class STMCoreSessionFiler(org: String, uid: String, var context:Context? = MyApplication.appContext) : STMFiling {

    private val userOrg = context!!.filesDir.absolutePath + "/" + org
    private val userDocuments = "$userOrg/$uid"
    private val persistenceBasePath = userDocuments + "/" + STMConstants.PERSISTENCE_PATH
    private val picturePath = "$userOrg/pictures"
    private val webPath = "$userOrg/Web/byName"

    override fun persistencePath(folderName: String): String {

        val path = "$persistenceBasePath/$folderName"

        File(path).mkdirs()

        return path

    }

    override fun webPath(title:String): String {

        val path = "$webPath/$title/localHTML"

        File(path).mkdirs()

        return path

    }

    override fun tempWebPath(title:String): String {

        val path = "$webPath/$title/tempHTML"

        File(path).mkdirs()

        return path

    }

    override fun saveImage(bitmap: Bitmap, folderName: String, fileName:String):String {

        val folder = File(picturePath, folderName)

        folder.mkdirs()

        val file = File(folder.absolutePath, fileName)

        val fOut = FileOutputStream(file)

        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut)
        fOut.flush()
        fOut.close()
//        MediaStore.Images.Media.insertImage(context!!.contentResolver, file.absolutePath, file.name, file.name)
        context?.openFileOutput(file.name, Context.MODE_PRIVATE).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 25, fos)
        }

        return "$folderName/$fileName"

    }

    override fun getImage(localPath: String): Bitmap? {

        return BitmapFactory.decodeFile("$picturePath/$localPath")

    }

    override fun removeOrgDirectory() {

        STMFunctions.deleteRecursive(File(userOrg))

    }

}