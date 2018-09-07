package com.sistemium.sissales.base.classes.entitycontrollers

import android.R.attr.*
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import java.io.File
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Environment
import android.os.Environment.DIRECTORY_DCIM
import android.os.Environment.getExternalStoragePublicDirectory
import android.content.Context.WINDOW_SERVICE
import android.graphics.Point
import android.view.Display
import android.view.WindowManager
import com.sistemium.sissales.base.MyApplication
import android.provider.MediaStore
import android.service.carrier.CarrierIdentifier
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.Blob
import com.github.kittinunf.fuel.core.DataPart
import com.github.kittinunf.fuel.core.FuelManager
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.persisting.STMPredicate
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.OutputStream
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


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

    fun setImagesFromData(file:Bitmap, picture:Map<*,*>, entityName:String):Map<*,*>{

        val xid = picture[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] as String
        val fileName = "$xid.png"

        STMFunctions.debugLog("STMCorePicturesController", "saveResized: $fileName")

        val resizedPath = saveResizedImageFile("resized_$fileName", file, entityName)

        val mutablePicture = HashMap(picture)

        STMFunctions.debugLog("STMCorePicturesController", "saveThumbnail: $fileName")

        val thumbnailPath = saveThumbnailImageFile("thumbnail_$fileName", file, entityName)

        mutablePicture["resizedImagePath"] = resizedPath

        mutablePicture["thumbnailPath"] = thumbnailPath

        mutablePicture["imagePath"] = thumbnailPath

        return mutablePicture.toMap()
    }

    fun saveImageFile(fileName:String, file:Bitmap, entityName:String):String{

        val bitmap = Bitmap.createBitmap(file)

        return STMSession.sharedSession!!.filing.saveImage(bitmap, entityName, fileName)

    }

    fun loadImageForPrimaryKey(identifier:String): Promise<Map<*, *>, Exception>{

        val predicate = STMPredicate("${STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY} = '$identifier'")

        val picture = allPicturesWithPredicate(predicate).firstOrNull()

        if (picture == null){

            STMLogger.sharedLogger!!.errorMessage("Picture not found id = $identifier")

            throw Exception("Picture not found")

        }

        val attributes = picture["attributes"] as Map<*,*>
        val entityName = picture["entityName"] as String

        return downloadImagesEntityName(entityName, attributes)

    }

    fun uploadImageEntityName(photoEntityName:String, attributes:Map<*,*>, image:Bitmap){

        val appSettings = STMSession.sharedSession!!.settingsController!!.currentSettingsForGroup("appSettings")
        val url = (appSettings!!["IMS.url"] as String) + "?folder="

        val (year, month, day) = STMFunctions.stringFrom(Date()).split(" ").first().split("-")

        val imsURL = "$url$photoEntityName%2F$year%2F$month%2F$day"

        Fuel.upload(imsURL).header(mapOf("Authorization" to STMCoreAuthController.accessToken!!))

                .dataParts { _, _ ->
                    val f = File.createTempFile("temp", ".tmp")

                    val fOut = FileOutputStream(f)

                    image.compress(Bitmap.CompressFormat.PNG, 100, fOut)
                    fOut.flush()
                    fOut.close()
                    listOf(
                            DataPart(f, "image", "image/png")
                    )
                }

                .responseString { request, response, result ->

                    if (response.statusCode != 200){

                        STMFunctions.debugLog("STMCorePicturesController Error", response.responseMessage)

                        return@responseString
                    }

                    val dictionary = STMFunctions.gson.fromJson(result.component1(), Map::class.java)

                    val picturesDicts = dictionary["pictures"] as ArrayList<*>

                    val picture = HashMap<String,Any?>()

                    for (dict in picturesDicts){

                        if ((dict as Map<*,*>)["name"] == "original"){

                            picture["href"] = dict["src"]

                        }

                    }

                    picture["picturesInfo"] = picturesDicts

                    val imagePath = attributes["imagePath"]

                    picture["imagePath"] = attributes["resizedImagePath"]

                    picture[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY] = attributes[STMConstants.DEFAULT_PERSISTING_PRIMARY_KEY]

                    STMSession.sharedSession!!.persistenceDelegate.merge(photoEntityName, picture, hashMapOf(STMConstants.STMPersistingOptionReturnSaved to false)) success {

                        removeImageFile(imagePath as String, photoEntityName)

                    } fail {

                        STMLogger.sharedLogger!!.importantMessage("Error on update after upload: ${it.localizedMessage}")

                    }


        }

    }

    private fun removeImageFile(filePath:String, entityName:String){

        //TODO

    }

    private fun downloadImagesEntityName(entityName:String, attributes:Map<*,*>): Promise<Map<*, *>, Exception> {

        //TODO

        return task {

            return@task attributes

        }

    }

    private fun allPicturesWithPredicate(predicate:STMPredicate):ArrayList<Map<*,*>>{

        val result = arrayListOf<Map<*,*>>()

        for (entityName in pictureEntitiesNames()){

            val objects = STMSession.sharedSession!!.persistenceDelegate.findAllSync(entityName, predicate, null)

            result.addAll(objects.map {

                return@map hashMapOf("entityName" to entityName, "attributes" to it)

            })

        }

        return result

    }

    private fun pictureEntitiesNames():Set<String>{

        return STMModelling.sharedModeler!!.hierarchyForEntityName("STMCorePicture")

    }

    private fun saveResizedImageFile(resizedFileName:String, file:Bitmap, entityName:String):String{

        val maxPictureScale = STMEntityController.sharedInstance!!.entityWithName(entityName)?.get("maxPictureScale") as? Double ?: 1.0

        val maxPictureDimension = Math.max(file.height, file.width)

        val display = (MyApplication.appContext!!.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay
        val size = Point()
        display.getSize(size)
        val screenWidth = size.x
        val screenHeight = size.y

        val maxScreenDimension = Math.max(screenWidth,screenHeight)

        val MAX_PICTURE_SIZE = maxScreenDimension * maxPictureScale

        val scale = if (maxPictureDimension > MAX_PICTURE_SIZE) MAX_PICTURE_SIZE / maxPictureDimension else 1.0

        val bitmap = Bitmap.createScaledBitmap(file, (file.width * scale).toInt(), (file.height * scale).toInt(), false)

        return STMSession.sharedSession!!.filing.saveImage(bitmap, entityName, resizedFileName)

    }

    private fun saveThumbnailImageFile(thumbnailFileName:String, file:Bitmap, entityName:String):String{

        val bitmap = Bitmap.createScaledBitmap(file, STMConstants.THUMBNAIL_SIZE, STMConstants.THUMBNAIL_SIZE, false)

        return STMSession.sharedSession!!.filing.saveImage(bitmap, entityName,  thumbnailFileName)

    }

}