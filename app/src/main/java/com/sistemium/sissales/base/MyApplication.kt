package com.sistemium.sissales.base

import android.app.Application
import android.content.Context

/**
 * Created by edgarjanvuicik on 02/02/2018.
 */
class MyApplication : Application() {

    companion object {

        var appContext: Context? = null
            private set

    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext

    }



}