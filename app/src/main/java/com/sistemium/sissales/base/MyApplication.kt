package com.sistemium.sissales.base

import android.app.Application
import android.content.ComponentCallbacks2
import android.content.Context
import com.sistemium.sissales.base.classes.entitycontrollers.STMCoreObjectsController
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.base.session.STMSyncer
import com.sistemium.sissales.enums.STMSocketEvent
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore

/**
 * Created by edgarjanvuicik on 02/02/2018.
 */
class MyApplication : Application() {

    companion object {

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

                    syncer?.sendEventViaSocket(STMSocketEvent.STMSocketEventStatusChange, logMessage)

                    STMCoreObjectsController.checkObjectsForFlushing()


                } else {

                    val logMessage = "application will enter foreground"

                    syncer?.sendEventViaSocket(STMSocketEvent.STMSocketEventStatusChange, logMessage)

                }

            }

        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)

        if (level == ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {

            inBackground = true

        }

    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

        val seedKey = "SecuredSeedData".toByteArray()
        SecuredPreferenceStore.init(applicationContext, null, null, seedKey, DefaultRecoveryHandler())

    }



}