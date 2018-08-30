package com.sistemium.sissales.base.classes.entitycontrollers

import android.app.Activity
import android.support.v4.app.ActivityCompat.startActivityForResult
import android.provider.MediaStore
import android.content.Intent
import com.sistemium.sissales.base.MyApplication


class STMCorePhotosController {

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


    private val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent(activity:Activity) {
        val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePictureIntent.resolveActivity(MyApplication.appContext!!.packageManager) != null) {
            startActivityForResult(activity,takePictureIntent, REQUEST_IMAGE_CAPTURE, null)
        }
    }

}