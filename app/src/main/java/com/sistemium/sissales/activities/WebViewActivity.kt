package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import com.sistemium.sissales.R
import com.sistemium.sissales.webInterface.WebAppInterface
import android.webkit.WebViewClient
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.R.id.webView1
import com.sistemium.sissales.base.STMFunctions
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import nl.komponents.kovenant.task


@SuppressLint("SetJavaScriptEnabled")
class WebViewActivity : Activity() {

    var webView: WebView? = null

    var manifestEtag:String?
        get() {

            val prefStore = SecuredPreferenceStore.getSharedInstance()
            return prefStore.getString("manifestEtag", null)

        }
        set(value) {

            val prefStore = SecuredPreferenceStore.getSharedInstance()

            prefStore.edit().putString("manifestEtag", value).apply()

        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)

        webView = findViewById(R.id.webView1)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true

        val webInterface = WebAppInterface(this)

        webView?.addJavascriptInterface(webInterface, "stmAndroid")

        val url = intent.getStringExtra("url")

        val manifest = intent.getStringExtra("manifest")

        task {

            if (manifest != null) {

                FuelManager.instance.baseHeaders = mapOf("user-agent" to "iSisSales/360", "DeviceUUID" to STMFunctions.deviceUUID(), "Authorization" to STMCoreAuthController.accessToken!!)

                val (_, response, result) = Fuel.get(manifest).responseJson()

                when (result) {
                    is Result.Success -> {

                        val responseETag = response.headers["ETag"]!!.first()

                        if (responseETag == manifestEtag){

                            runOnUiThread{

                                webView?.settings!!.cacheMode = WebSettings.LOAD_CACHE_ONLY

                            }

                        } else {

                            manifestEtag = responseETag

                            runOnUiThread{

                                webView?.settings!!.cacheMode = WebSettings.LOAD_NO_CACHE

                            }

                        }

                    }
                    else -> {

                        runOnUiThread{

                            webView?.settings!!.cacheMode = WebSettings.LOAD_CACHE_ONLY

                        }

                    }
                }

            }

            runOnUiThread{

                val appCachePath = applicationContext.cacheDir
                        .absolutePath

                webView?.settings?.setAppCachePath(appCachePath)
                webView?.settings?.allowFileAccess = true
                webView?.settings?.setAppCacheEnabled(true)

                webView?.loadUrl(url)

            }

        }

    }

    override fun onBackPressed() {

    }

    fun goBack() {

        runOnUiThread {

            super.onBackPressed()

        }

    }

}