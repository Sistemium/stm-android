package com.sistemium.sissales

import android.content.Context
import android.icu.util.ULocale
import android.webkit.JavascriptInterface
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson
import java.util.*
import com.google.gson.internal.LinkedTreeMap




/**
 * Created by edgarjanvuicik on 27/10/2017.
 */

class WebAppInterface internal constructor(internal var mContext: Context) {

    var gson = Gson()

    @JavascriptInterface
    fun post(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun get(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun barCodeScannerOn(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun findAll(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun find(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun update(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun updateAll(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun destroy(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun sound(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun tabbar(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun subscribe(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

    @JavascriptInterface
    fun remoteControl(options: String?){

        val mapOptions = gson.fromJson(options, Map::class.java)

        print("test")

    }

}
