package com.sistemium.sissales.base.classes.entitycontrollers

import android.os.Environment
import com.sistemium.sissales.BuildConfig
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.interfaces.STMFullStackPersisting
import android.os.StatFs

/**
 * Created by edgarjanvuicik on 20/02/2018.
 */
class STMClientDataController {

    companion object {

        val clientData: Map<*, *>
            get() {

                val entityName = "STMClientData"

                val fetchResult = persistenceDelegate!!.findAllSync(entityName, null, null)

                var clientData = fetchResult.lastOrNull()

                if (clientData == null) {

                    clientData = hashMapOf<Any, Any>()

                }

                return clientData

            }

        var persistenceDelegate: STMFullStackPersisting? = null

        private var bundleIdentifier:String = ""
            get() {
                return STMConstants.BUNDLE_DISPLAY_NAME
            }

        private var appVersion:String = ""
            get() {
                return "${BuildConfig.VERSION_CODE}"
            }

        private var bundleVersion:String = ""
            get() {
                return BuildConfig.VERSION_NAME
            }

        private var buildType:String = ""
            get() {
                return if (BuildConfig.DEBUG){

                    "debug"

                } else {

                    "release"

                }
            }

        private var deviceName:String = ""
            get() {
                return android.os.Build.MODEL
            }

        private var lastAuth:String = ""
            get() {
                return STMCoreAuthController.lastAuth ?: ""
            }

        private var devicePlatform:String = ""
            get() {
                return android.os.Build.MODEL
            }

        private var systemVersion:String = ""
            get() {
                return android.os.Build.VERSION.RELEASE
            }

        private var deviceUUID:String = ""
            get() {
                return STMFunctions.deviceUUID()
            }

        private var freeDiskSpace:String = ""
            get() {

                val pathInternal = Environment.getDataDirectory()
                val statInternal = StatFs(pathInternal.path)

                return "${statInternal.availableBlocksLong * statInternal.blockSizeLong / 1024.0 / 1024.0}"

            }

        fun checkClientData(){

            var haveUpdates = false

            val clientData = HashMap(clientData)

            if (clientData["bundleIdentifier"] != bundleIdentifier){

                clientData["bundleIdentifier"] = bundleIdentifier

                haveUpdates = true

            }

            if (clientData["appVersion"] != appVersion){

                clientData["appVersion"] = appVersion

                haveUpdates = true

            }

            if (clientData["bundleVersion"] != bundleVersion){

                clientData["bundleVersion"] = bundleVersion

                haveUpdates = true

            }

            if (clientData["buildType"] != buildType){

                clientData["buildType"] = buildType

                haveUpdates = true

            }

            if (clientData["appVersion"] != appVersion){

                clientData["appVersion"] = appVersion

                haveUpdates = true

            }

            if (clientData["deviceName"] != deviceName){

                clientData["deviceName"] = deviceName

                haveUpdates = true

            }

            if (clientData["lastAuth"] != lastAuth){

                clientData["lastAuth"] = lastAuth

                haveUpdates = true

            }

            if (clientData["devicePlatform"] != devicePlatform){

                clientData["devicePlatform"] = devicePlatform

                haveUpdates = true

            }

            if (clientData["systemVersion"] != systemVersion){

                clientData["systemVersion"] = systemVersion

                haveUpdates = true

            }

            if (clientData["deviceUUID"] != deviceUUID){

                clientData["deviceUUID"] = deviceUUID

                haveUpdates = true

            }

            if (clientData["freeDiskSpace"] != freeDiskSpace){

                clientData["freeDiskSpace"] = freeDiskSpace

                haveUpdates = true

            }

            if (haveUpdates){

                persistenceDelegate!!.mergeSync("STMClientData", clientData, null)

            }

        }

    }

}