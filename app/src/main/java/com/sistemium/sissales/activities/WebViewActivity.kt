package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import com.sistemium.sissales.model.STMModeller
import com.sistemium.sissales.R

@SuppressLint("SetJavaScriptEnabled")
class WebViewActivity : Activity() {

    var webView: WebView? = null

    var accessToken:String? = null

    var roles:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)

        webView = findViewById(R.id.webView1)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true

        val intent = intent

        accessToken = intent.getStringExtra("accessToken")

        webView?.addJavascriptInterface(WebAppInterface(this), "stmAndroid")

        val modeller = STMModeller(this,"iSisSales")

        webView?.loadUrl("http://10.0.1.2:3000")


    }
}
