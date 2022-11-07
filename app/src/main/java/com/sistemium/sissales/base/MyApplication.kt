package com.sistemium.sissales.base

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import com.sistemium.sissales.base.classes.entitycontrollers.STMCoreObjectsController
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePicturesController
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.base.session.STMSyncer
import com.sistemium.sissales.enums.STMSocketEvent
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.embedding.engine.FlutterEngineCache
import io.flutter.embedding.engine.dart.DartExecutor
import io.flutter.plugin.common.MethodChannel

/**
 * Created by edgarjanvuicik on 02/02/2018.
 */
class MyApplication : Application(), Application.ActivityLifecycleCallbacks {

    companion object {

        lateinit var flutterEngine: FlutterEngine
        lateinit var channel: MethodChannel


        var syncer: STMSyncer? = null
            get() {

                return STMSession.sharedSession?.syncer

            }

        var appContext: Context? = null
            private set

        var inBackground: Boolean = false
        set(value) {

            if (STMCoreAuthController.accessToken == null) return

            if (field != value) {

                field = value

                if (value) {

                    val logMessage = "application did enter background"

                    STMLogger.sharedLogger!!.infoMessage(logMessage)

                    syncer?.sendEventViaSocket(STMSocketEvent.STMSocketEventStatusChange, logMessage)

                    STMCoreObjectsController.checkObjectsForFlushing()

                    //TODO
                    //[STMGarbageCollector.sharedInstance removeOutOfDateImages];

                } else {

                    val logMessage = "application will enter foreground"

                    STMLogger.sharedLogger!!.infoMessage(logMessage)

                    syncer?.sendEventViaSocket(STMSocketEvent.STMSocketEventStatusChange, logMessage)

                }

            }

        }
    }

    override fun onActivityPaused(p0: Activity) {
        inBackground = true
    }

    override fun onActivityStarted(p0: Activity) {
    }

    override fun onActivityDestroyed(p0: Activity) {
    }

    override fun onActivitySaveInstanceState(p0: Activity, p1: Bundle) {
    }

    override fun onActivityStopped(p0: Activity) {
    }

    override fun onActivityCreated(p0: Activity, p1: Bundle?) {
    }

    override fun onActivityResumed(p0: Activity) {

        inBackground = false

    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        val seedKey = "SecuredSeedData".toByteArray()
        SecuredPreferenceStore.init(applicationContext, null, null, seedKey, DefaultRecoveryHandler())

        this.registerActivityLifecycleCallbacks(this)

        flutterEngine = when (STMCoreAuthController.configuration) {
            "vfs" -> {
                FlutterEngine(this, arrayOf("vfs"))
            }
            "vfsc" -> {
                FlutterEngine(this, arrayOf("vfsClient"))
            }
            else -> {
                FlutterEngine(this, arrayOf("iSisSales"))
            }
        }

        // Start executing Dart code to pre-warm the FlutterEngine.
        flutterEngine.dartExecutor.executeDartEntrypoint(
                DartExecutor.DartEntrypoint.createDefault()
        )

        // Cache the FlutterEngine to be used by FlutterActivity.
        FlutterEngineCache
                .getInstance()
                .put("my flutter engine", flutterEngine)

        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.sistemium.flutterchanel")

        channel.setMethodCallHandler {
            call, _ ->
            if (call.method == "startSyncer"){
                STMSession.sharedSession!!.setupSyncer()
            }
            if (call.method == "completeAuth"){
                val arguments = call.arguments as HashMap<*, *>
                STMCoreAuthController.userName = arguments["userName"] as? String
                STMCoreAuthController.phoneNumber = arguments["phoneNumber"] as? String
                STMCoreAuthController.accessToken = arguments["accessToken"] as? String
                STMCoreAuthController.userID = arguments["userID"] as? String
                STMCoreAuthController.entityResource = arguments["redirectUri"] as? String
                STMCoreAuthController.socketURL = arguments["socketURL"] as? String
                STMCoreAuthController.accountOrg = arguments["accountOrg"] as? String
                STMCoreAuthController.iSisDB = arguments["iSisDB"] as? String
                STMCoreAuthController.stcTabs = arguments["stcTabs"] as? ArrayList<*>
                STMCoreAuthController.rolesResponse = arguments["rolesResponse"] as? Map<*,*>
            }
            if (call.method == "logout"){
                STMCoreAuthController.logout()
            }
            if (call.method == "syncData"){
                STMSession.sharedSession?.syncer?.receiveData()
            }
            if (call.method == "startImageDownload"){
                STMCorePicturesController.sharedInstance?.checkNotUploadedPhotos()
            }
            if (call.method == "stopImageDownload"){
            }

//            if ([call.method isEqual: @"loadURL"]){
//            NSDictionary * arguments = [call arguments];
//            STMStoryboard *storyboard = [STMStoryboard storyboardWithName:@"STMWKWebView" bundle:nil];
//            storyboard.parameters = arguments;
//            self.window.rootViewController = [storyboard instantiateInitialViewController];
//        }
        }

    }



}