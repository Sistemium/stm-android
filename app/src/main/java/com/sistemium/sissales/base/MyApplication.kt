package com.sistemium.sissales.base

import android.app.Activity
import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import android.os.Bundle
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMCoreSessionManager
import com.sistemium.sissales.base.session.STMSyncer
import com.sistemium.sissales.enums.STMSocketEvent
import com.sistemium.sissales.interfaces.STMAdapting
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import kotlin.properties.Delegates

/**
 * Created by edgarjanvuicik on 02/02/2018.
 */
class MyApplication : Application(), Application.ActivityLifecycleCallbacks, ComponentCallbacks2 {

    companion object {

        var testAdapter: STMAdapting? = null

        var syncer: STMSyncer? = null
            get() {

                return STMCoreSessionManager.sharedManager.currentSession?.syncer

            }

        var appContext: Context? = null
            private set

        var inBackground : Boolean by Delegates.observable(false) {
            _, oldValue, newValue ->
            if (oldValue != newValue){

                if (newValue) {

                    val logMessage = "application did enter background"

                    STMLogger.sharedLogger.infoMessage(logMessage)

                    syncer?.sendEventViaSocket(STMSocketEvent.STMSocketEventStatusChange, logMessage)

                    //TODO
                    //[STMGarbageCollector.sharedInstance removeOutOfDateImages];

                } else {

                    val logMessage = "application will enter foreground"

                    STMLogger.sharedLogger.infoMessage(logMessage)

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
        SecuredPreferenceStore.init(appContext, DefaultRecoveryHandler())
        this.registerActivityLifecycleCallbacks(this)
    }
    override fun onActivityPaused(p0: Activity?) {}
    override fun onActivityStarted(p0: Activity?) {}
    override fun onActivityDestroyed(p0: Activity?) {}
    override fun onActivitySaveInstanceState(p0: Activity?, p1: Bundle?) {}
    override fun onActivityStopped(p0: Activity?) {}
    override fun onActivityCreated(p0: Activity?, p1: Bundle?) {}

}