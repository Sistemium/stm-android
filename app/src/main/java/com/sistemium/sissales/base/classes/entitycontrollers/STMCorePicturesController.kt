package com.sistemium.sissales.base.classes.entitycontrollers

import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.os.Environment.getExternalStoragePublicDirectory
import android.R.attr.y
import android.R.attr.x
import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.view.Display
import android.view.WindowManager
import com.sistemium.sissales.base.MyApplication
import android.provider.MediaStore
import com.sistemium.sissales.base.session.STMSession
import java.io.OutputStream


/**
 * Created by edgarjanvuicik on 02/03/2018.
 */
class STMCorePicturesController {

    companion object {

        private var INSTANCE:STMCorePicturesController? = null

        var sharedInstance: STMCorePicturesController?
            get() {

                if (INSTANCE == null){

                    INSTANCE = STMCorePicturesController()

                }

                return INSTANCE!!

            }
            set(value) {

                INSTANCE = value

            }

    }

    fun checkNotUploadedPhotos() {

        //TODO not implemented

    }

    fun setImagesFromData(file:String, picture:Map<*,*>, entityName:String):Map<*,*>{

        val xid = picture[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as String
        val fileName = "$xid.jpg"
        val result = true

        STMFunctions.debugLog("STMCorePicturesController", "saveResized: $fileName")

        val resizedPath = saveResizedImageFile("resized_$fileName", file, entityName)

        val mutablePicture = HashMap(picture)

        mutablePicture["resizedImagePath"] = resizedPath

        STMFunctions.debugLog("STMCorePicturesController", "saveThumbnail: $fileName")

        val thumbnailPath = saveThumbnailImageFile("thumbnail_$fileName", file, entityName)

        mutablePicture["thumbnailPath"] = thumbnailPath

        mutablePicture["imagePath"] = thumbnailPath

        return mutablePicture.toMap()
    }

    fun saveImageFile(fileName:String, file:String, entityName:String):String{

        val image = BitmapFactory.decodeFile(file)

        val bitmap = Bitmap.createBitmap(image)

        return STMSession.sharedSession!!.filing.saveImage(bitmap, entityName, fileName)

    }

    fun uploadImageEntityName(photoEntityName:String, attributes:Map<*,*>, image:String){



    }

    private fun saveResizedImageFile(resizedFileName:String, file:String, entityName:String):String{

        val maxPictureScale = STMEntityController.sharedInstance!!.entityWithName(entityName)?.get("maxPictureScale") as? Double ?: 1.0

        val image = BitmapFactory.decodeFile(file)

        val maxPictureDimension = Math.max(image.height, image.width)

        val display = (MyApplication.appContext!!.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        val screenWidth = size.x
        val screenHeight = size.y

        val maxScreenDimension = Math.max(screenWidth,screenHeight)

        val MAX_PICTURE_SIZE = maxScreenDimension * maxPictureScale

        val scale = if (maxPictureDimension > MAX_PICTURE_SIZE) MAX_PICTURE_SIZE / maxPictureDimension else 1.0

        val bitmap = Bitmap.createScaledBitmap(image, (image.width * scale).toInt(), (image.height * scale).toInt(), false)

        return STMSession.sharedSession!!.filing.saveImage(bitmap, entityName, resizedFileName)

    }

    private fun saveThumbnailImageFile(thumbnailFileName:String, file:String, entityName:String):String{

        val image = BitmapFactory.decodeFile(file)

        val bitmap = Bitmap.createScaledBitmap(image, STMConstants.THUMBNAIL_SIZE, STMConstants.THUMBNAIL_SIZE, false)

        return STMSession.sharedSession!!.filing.saveImage(bitmap, entityName,  thumbnailFileName)

    }

}