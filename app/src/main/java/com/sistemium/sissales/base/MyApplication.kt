package com.sistemium.sissales.base

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.os.Bundle
import com.crashlytics.android.Crashlytics
import com.sistemium.sissales.activities.ProfileActivity
import com.sistemium.sissales.base.classes.entitycontrollers.STMCoreObjectsController
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.base.session.STMSyncer
import com.sistemium.sissales.enums.STMSocketEvent
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import io.fabric.sdk.android.Fabric
import nl.komponents.kovenant.task

/**
 * Created by edgarjanvuicik on 02/02/2018.
 */
class MyApplication : Application(), Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    companion object {

        var syncer: STMSyncer? = null
            get() {

                return STMSession.sharedSession?.syncer

            }

        var appContext: Context? = null
            private set

        var inBackground: Boolean = false
        set(value) {

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

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {

            MyApplication.inBackground = true

        }

    }

    override fun onActivityResumed(p0: Activity?) {

        MyApplication.inBackground = false

    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        val seedKey = "SecuredSeedData".toByteArray()
        SecuredPreferenceStore.init(applicationContext, null, null, seedKey, DefaultRecoveryHandler())

        this.registerActivityLifecycleCallbacks(this)
        Fabric.with(this, Crashlytics())

    }

    override fun onActivityPaused(p0: Activity?) {}
    override fun onActivityStarted(p0: Activity?) {}
    override fun onActivityDestroyed(p0: Activity?) {}
    override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {}
    override fun onActivityStopped(p0: Activity?) {}
    override fun onActivityCreated(p0: Activity?, p1: Bundle?) {}

}