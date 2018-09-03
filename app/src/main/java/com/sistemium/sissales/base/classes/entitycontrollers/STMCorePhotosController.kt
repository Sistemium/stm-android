package com.sistemium.sissales.base.classes.entitycontrollers

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.provider.MediaStore
import android.content.Intent
import com.sistemium.sissales.base.MyApplication
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.content.DialogInterface
import android.net.Uri
import android.os.Environment
import android.webkit.ValueCallback
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMSession
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class STMCorePhotosController {

    companion object {

        private var INSTANCE:STMCorePhotosController? = null

        var sharedInstance: STMCorePhotosController?
            get() {

                if (INSTANCE == null){

                    INSTANCE = STMCorePhotosController()

                }

                return INSTANCE!!

            }
            set(value) {

                INSTANCE = value

            }

    }

    fun selectImage(activity: Activity):String? {

        var mCM: String? = null

        var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent!!.resolveActivity(activity.packageManager) != null) {
            var photoFile: File? = null
            try {
                photoFile = createImageFile()
                takePictureIntent.putExtra("PhotoPath", mCM)
            } catch (ex: IOException) {
                STMFunctions.debugLog("WebViewActivity", "Image file creation failed")
            }

            if (photoFile != null) {
                mCM = photoFile.absolutePath
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
            } else {
                takePictureIntent = null
            }
        }
        val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
        contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
        contentSelectionIntent.type = "*/*"
        val intentArray: Array<Intent?>
        intentArray = if (takePictureIntent != null) {
            arrayOf(takePictureIntent)
        } else {
            arrayOfNulls(0)
        }
        val chooserIntent = Intent(Intent.ACTION_CHOOSER)
        chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
        chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
        startActivityForResult(activity, chooserIntent, 1, null)

        return mCM

    }

    fun newPhotoObject(photoEntityName:String, file:String):Map<*,*>{

        val picture = STMCorePicturesController.sharedInstance!!.setImagesFromData(file, hashMapOf(STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY to STMFunctions.uuidString()), photoEntityName)

        val xid = picture[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as String

        val fileName = "$xid.jpg"

        val path = STMCorePicturesController.sharedInstance!!.saveImageFile(fileName, file, photoEntityName)

        val mutablePicture = HashMap(picture)

        mutablePicture["imagePath"] = path

        return mutablePicture.toMap()

    }

    fun uploadPhotoEntityName(photoEntityName:String, attributes:Map<*,*>, image:String){

        STMCorePicturesController.sharedInstance!!.uploadImageEntityName(photoEntityName, attributes, image)

    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        @SuppressLint("SimpleDateFormat") val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

}