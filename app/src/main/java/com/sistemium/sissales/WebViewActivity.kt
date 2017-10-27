package com.sistemium.sissales

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled")
class WebViewActivity : Activity() {

    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)

        webView = findViewById(R.id.webView1)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true

        webView?.addJavascriptInterface(WebAppInterface(this), "stmAndroid")

        webView?.loadUrl("http://10.0.1.2:3000")


    }
}
