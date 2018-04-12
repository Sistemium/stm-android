package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
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

    companion object {

        var webInterface: WebAppInterface? = null

    }

    var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        WebView.setWebContentsDebuggingEnabled(true)

        webView = findViewById(R.id.webView1)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true

        webInterface = WebAppInterface(this)

        webView?.addJavascriptInterface(webInterface, "stmAndroid")

        val url = intent.getStringExtra("url")

        task {

            runOnUiThread {

                webView?.settings!!.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

                val appCachePath = applicationContext.cacheDir
                        .absolutePath

                webView?.settings?.setAppCachePath(appCachePath)
                webView?.settings?.allowFileAccess = true
                webView?.settings?.setAppCacheEnabled(true)
                webView?.settings?.mixedContentMode = MIXED_CONTENT_ALWAYS_ALLOW

                webView?.webViewClient = object : WebViewClient() {

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }

                }

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