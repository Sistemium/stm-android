package com.sistemium.sissales.base.classes.entitycontrollers

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

}