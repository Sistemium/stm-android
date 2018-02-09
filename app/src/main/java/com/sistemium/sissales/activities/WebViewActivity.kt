package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import com.sistemium.sissales.R
import com.sistemium.sissales.WebInterface.WebAppInterface

@SuppressLint("SetJavaScriptEnabled")
class WebViewActivity : Activity() {

    var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)

        webView = findViewById(R.id.webView1)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true

        val webInterface = WebAppInterface(this)

        webView?.addJavascriptInterface(webInterface, "stmAndroid")

        webView?.loadUrl("http://10.0.1.5:3000")


    }
}