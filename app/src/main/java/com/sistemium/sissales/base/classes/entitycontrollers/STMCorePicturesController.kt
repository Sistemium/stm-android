package com.sistemium.sissales.base.classes.entitycontrollers

/**
 * Created by edgarjanvuicik on 02/03/2018.
 */
class STMCorePicturesController {

    private object Holder {
        val INSTANCE = STMCorePicturesController()
    }

    companion object {

        val sharedInstance: STMCorePicturesController by lazy { Holder.INSTANCE }

    }

    fun checkNotUploadedPhotos() {

        //TODO not implemented

    }

}