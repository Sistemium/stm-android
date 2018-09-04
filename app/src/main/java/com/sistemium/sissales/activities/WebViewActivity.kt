package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import com.sistemium.sissales.R
import com.sistemium.sissales.webInterface.WebAppInterface
import com.sistemium.sissales.base.STMFunctions
import nl.komponents.kovenant.task
import android.content.Intent
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePhotosController
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMSession
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import java.io.File


@SuppressLint("SetJavaScriptEnabled")
class WebViewActivity : Activity() {

    companion object {

        var webInterface: WebAppInterface? = null

    }

    var webView: WebView? = null
    var filePath:String? = null
    var photoMapParameters:Map<*,*>? = null
    private var mUMA:ValueCallback<Array<Uri>>? = null
    private var err:String?  = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        WebView.setWebContentsDebuggingEnabled(true)

        webView = findViewById(R.id.webView1)
        webView?.settings?.javaScriptEnabled = true

        webView?.settings?.domStorageEnabled = true

        webView?.settings?.allowFileAccess = true
        webView?.settings?.allowContentAccess = true
        webView?.settings?.allowUniversalAccessFromFileURLs = true
        webView?.settings?.allowFileAccessFromFileURLs = true

        webInterface = WebAppInterface(this)

        webView?.addJavascriptInterface(webInterface, "stmAndroid")
        webView?.settings?.mediaPlaybackRequiresUserGesture = false
        webView?.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {

                if (mUMA != null) {
                    mUMA!!.onReceiveValue(null)
                }
                mUMA = filePathCallback

                filePath = STMCorePhotosController.sharedInstance!!.selectImage(this@WebViewActivity)

                return true

            }

            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                if (consoleMessage!!.message().startsWith("Uncaught Error")){
                    err = consoleMessage.message()
                }
                return super.onConsoleMessage(consoleMessage)
            }

        }

        val url = intent.getStringExtra("url")
        val manifest = intent.getStringExtra("manifest")
        val title = intent.getStringExtra("title")

        task {

            runOnUiThread {

                webView?.settings!!.cacheMode = WebSettings.LOAD_CACHE_ELSE_NETWORK

                val appCachePath = applicationContext.cacheDir
                        .absolutePath

                webView?.settings?.setAppCachePath(appCachePath)
                webView?.settings?.allowFileAccess = true
                webView?.settings?.setAppCacheEnabled(true)


                webView?.webViewClient = object : WebViewClient() {

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        err = error.toString()
                        STMFunctions.debugLog("CHROME", error.toString())
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }

                }

                loadFromManifest(manifest, title, url).success {

                    runOnUiThread{

                        webView?.loadUrl("file:///$it")

                    }

                }fail {

                    val test = ""

                }
                
            }

        }

    }

    override fun onBackPressed() {

        if (err != null){

            this.goBack()

        } else {

            webView?.evaluateJavascript("window.history.back()"){

                STMFunctions.debugLog("DEBUG", "Evaluate finish")

            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        var nativePath = ""

        var results: Array<Uri>? = null
        if (resultCode === Activity.RESULT_OK) {
            if (requestCode === 1) {
                if (null == mUMA && photoMapParameters == null) {
                    return
                }
                if (data == null || data.data == null) {
                    if (filePath != null) {
                        results = arrayOf(Uri.parse("file:$filePath"))
                        nativePath = filePath.toString()
                    }
                } else {
                    val dataString = data.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse("file:$dataString"))
                        nativePath = dataString
                    }
                }
            }
        }

        if (photoMapParameters != null) {

            val photoEntityName = photoMapParameters!!["entityName"] as String

            val attributes = HashMap(STMCorePhotosController.sharedInstance!!.newPhotoObject(photoEntityName, nativePath))

            val photoData = photoMapParameters!!["data"] as Map<*,*>

            attributes.putAll(photoData)

            STMSession.sharedSession!!.persistenceDelegate.merge(photoEntityName, attributes.toMap(), null).then {

                val callback = photoMapParameters!!["callback"] as String
                webInterface!!.javascriptCallback(arrayListOf(it), photoMapParameters, callback)

                STMCorePhotosController.sharedInstance!!.uploadPhotoEntityName(photoEntityName, attributes, nativePath)

            }.fail {

                STMLogger.sharedLogger!!.importantMessage(it.localizedMessage)

                webInterface!!.javascriptCallback(it, photoMapParameters)

            }

            return

        }

        mUMA?.onReceiveValue(results)
        mUMA = null

    }

    fun goBack() {
        
        runOnUiThread {

            webView?.destroy()

            super.onBackPressed()

        }

    }

    private fun filePathsToLoadFromAppManifest(manifest:String):List<String>{

        return manifest.split("\n").map {

            return@map it.filter{

                if (it == ' '){

                    return@filter false

                }

                return@filter true

            }

        }.filter {

            if (it == "" || it.startsWith("#") || it == "favicon.ico" || it == "robots.txt"
                    || it == "CACHEMANIFEST"|| it == "CACHE:"|| it == "NETWORK:"|| it == "*"){

                return@filter false

            }

            return@filter true

        }

    }

    fun loadFromManifest(manifest:String, title:String, url:String): Promise<String, Exception> {

        return task {

            val webPath = STMSession.sharedSession!!.filing.webPath(title)

            val (_, response, result) = Fuel.get(manifest).responseJson()

            val etag = response.headers["ETag"]?.first()

            val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

            val savedTag = prefStore?.getString("${title}ETag", null)

            if (etag != savedTag && etag != null){

                prefStore?.edit()?.putString("${title}ETag", etag)?.apply()

                STMFunctions.debugLog("STMCoreAuthController","update available")

                val filePaths = filePathsToLoadFromAppManifest(result.get().content)

                for (file in filePaths){

                    Fuel.download("$url/$file").destination { _, _ ->

                        File.createTempFile("temp", ".tmp")

                     }.response { req, res, result ->

                        val path = "$webPath/$file"

                        File(path.removeSuffix("/" + path.split("/").last())).mkdirs()

                        if (result.component1()!!.isNotEmpty()){

                            File("$webPath/$file").writeBytes(result.component1()!!)

                        }

                    }

                }

            }



            return@task "$webPath/index.html"

        }


    }

}