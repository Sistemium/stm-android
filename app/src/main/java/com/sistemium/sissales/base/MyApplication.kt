package com.sistemium.sissales.base

import android.app.Activity
import android.app.ActivityOptions
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.sistemium.sissales.R
import com.sistemium.sissales.activities.WebViewActivity
import com.sistemium.sissales.base.classes.entitycontrollers.STMCoreObjectsController
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePicturesController
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.base.session.STMSyncer
import com.sistemium.sissales.enums.STMSocketEvent
import com.sistemium.sissales.persisting.STMPredicate
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

                        syncer?.sendEventViaSocket(
                            STMSocketEvent.STMSocketEventStatusChange,
                            logMessage
                        )

                        STMCoreObjectsController.checkObjectsForFlushing()

                        //TODO
                        //[STMGarbageCollector.sharedInstance removeOutOfDateImages];

                    } else {

                        val logMessage = "application will enter foreground"

                        STMLogger.sharedLogger!!.infoMessage(logMessage)

                        syncer?.sendEventViaSocket(
                            STMSocketEvent.STMSocketEventStatusChange,
                            logMessage
                        )

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
        SecuredPreferenceStore.init(
            applicationContext,
            null,
            null,
            seedKey,
            DefaultRecoveryHandler()
        )

        this.registerActivityLifecycleCallbacks(this)

        flutterEngine = FlutterEngine(this)

        val entryPoints = when (STMCoreAuthController.configuration) {
            "vfs" -> {
                arrayOf("vfs")
            }
            "vfsc" -> {
                arrayOf("vfsClient")
            }
            else -> {
                arrayOf("SisSales")
            }
        }.toList()

        // Start executing Dart code to pre-warm the FlutterEngine.
        flutterEngine.dartExecutor.executeDartEntrypoint(
            DartExecutor.DartEntrypoint.createDefault(),
            entryPoints,
        )

        // Cache the FlutterEngine to be used by FlutterActivity.
        FlutterEngineCache
            .getInstance()
            .put("my flutter engine", flutterEngine)

        channel =
            MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "com.sistemium.flutterchanel")

        channel.invokeMethod("ensureUpgrade", hashMapOf(
            "userName" to STMCoreAuthController.userName,
            "phoneNumber" to STMCoreAuthController.phoneNumber,
            "accessToken" to STMCoreAuthController.accessToken,
            "id" to STMCoreAuthController.userID,
            "redirectUri" to STMCoreAuthController.entityResource,
            "apiUrl" to STMCoreAuthController.socketURL,
            "accountOrg" to STMCoreAuthController.accountOrg,
            "iSisDB" to STMCoreAuthController.iSisDB,
            "stcTabs" to STMCoreAuthController.stcTabs,
            "rolesResponse" to STMCoreAuthController.rolesResponse
        ))

        channel.setMethodCallHandler { call, _ ->
            if (call.method == "startSyncer") {
                STMSession.sharedSession!!.setupSyncer()
            }
            if (call.method == "completeAuth") {
                val arguments = call.arguments as HashMap<*, *>
                STMCoreAuthController.userName = arguments["userName"] as? String
                STMCoreAuthController.phoneNumber = arguments["phoneNumber"] as? String
                STMCoreAuthController.accessToken = arguments["accessToken"] as? String
                STMCoreAuthController.userID = arguments["id"] as? String
                STMCoreAuthController.entityResource = arguments["redirectUri"] as? String
                if (arguments["apiUrl"] as? String != ""){
                    STMCoreAuthController.socketURL = arguments["apiUrl"] as? String
                }
                STMCoreAuthController.accountOrg = arguments["accountOrg"] as? String
                if (arguments["iSisDB"] as? String != ""){
                    STMCoreAuthController.iSisDB = arguments["iSisDB"] as? String
                }
                STMCoreAuthController.stcTabs = arguments["stcTabs"] as? ArrayList<*>
                STMCoreAuthController.rolesResponse = arguments["rolesResponse"] as? Map<*, *>
                STMCoreAuthController.isDemo = arguments["isDemo"] as Boolean
            }
            if (call.method == "logout") {
                STMCoreAuthController.logout()
            }
            if (call.method == "syncData") {
                STMSession.sharedSession?.syncer?.receiveData()
            }
            if (call.method == "startImageDownload") {
                STMCorePicturesController.sharedInstance?.checkNotUploadedPhotos()
            }
            if (call.method == "stopImageDownload") {
            }
            if (call.method == "loadURL") {

                val currentTab = call.arguments as HashMap<*, *>

                val intent = Intent(appContext, WebViewActivity::class.java)

                var url = currentTab["url"] as? String

                val manifest = currentTab["appManifestURI"] as? String

                if (url == null && manifest != null) {

                    url = manifest.replace(manifest.split("/").last(), "")

                }

                if (url?.endsWith("/") != true) {
                    url += "/"
                }

                intent.putExtra("url", url)
                intent.putExtra("manifest", manifest)
                intent.putExtra("title", currentTab["title"] as String)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                val options = ActivityOptions.makeCustomAnimation(
                    appContext,
                    R.anim.abc_fade_in,
                    R.anim.abc_fade_out
                )

                applicationContext.startActivity(intent, options.toBundle())
            }
            if (call.method == "findWithSocket") {
                val arguments = call.arguments as HashMap<*, *>
                WebViewActivity.webInterface?.findWithSocket(hashMapOf<Any, Any>(), arguments["entity"] as String, STMPredicate(arguments["predicate"] as String), null)
            }
        }

    }


}