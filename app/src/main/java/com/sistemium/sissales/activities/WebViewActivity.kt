package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import com.sistemium.sissales.R
import com.sistemium.sissales.interfaces.STMModelling
import com.sistemium.sissales.model.STMModeller

@SuppressLint("SetJavaScriptEnabled")
class WebViewActivity : Activity() {

    var webView: WebView? = null

    var accessToken:String? = null

    var roles:String? = null

    var modellingDelegate: STMModelling? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        modellingDelegate = STMModeller(this, "iSisSales")


        webView = findViewById(R.id.webView1)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true

        val intent = intent

        accessToken = intent.getStringExtra("accessToken")

        val webInterface = WebAppInterface(this)

        webView?.addJavascriptInterface(webInterface, "stmAndroid")

        webView?.loadUrl("http://10.0.1.2:3000")


    }
}
