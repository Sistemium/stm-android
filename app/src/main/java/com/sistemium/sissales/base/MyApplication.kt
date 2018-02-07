package com.sistemium.sissales.base

import android.app.Application
import android.content.Context
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore

/**
 * Created by edgarjanvuicik on 02/02/2018.
 */
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
        SecuredPreferenceStore.init(appContext, DefaultRecoveryHandler())
    }

    companion object {

        var appContext: Context? = null
            private set

    }

}