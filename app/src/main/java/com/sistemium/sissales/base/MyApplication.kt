package com.sistemium.sissales.base

import android.app.ActivityOptions
import android.app.Application
import android.content.Context
import android.content.Intent
import com.sistemium.sissales.activities.WebViewActivity
import com.sistemium.sissales.base.session.STMCoreAuthController
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import com.sistemium.sissales.R
import com.sistemium.sissales.activities.AuthActivity

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