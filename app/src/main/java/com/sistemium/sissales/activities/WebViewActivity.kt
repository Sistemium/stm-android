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
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePhotosController
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMSession
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.then
import java.io.File
import android.support.v4.os.HandlerCompat.postDelayed




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
    private val updateHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {

        STMFunctions.memoryFix()

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

        initUpdater()

        task {

            runOnUiThread {


                webView?.webViewClient = object : WebViewClient() {

                    override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                        err = error.toString()
                        STMFunctions.debugLog("CHROME", error.toString())
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }

                }

                val webPath = STMSession.sharedSession!!.filing.webPath(title)+"/index.html"

                val webFolder = File(webPath)

                var needLoad = false

                if (webFolder.exists()){

                    webView?.loadUrl("file:///$webPath")

                } else {

                    needLoad = true

                }

                loadFromManifest(manifest, title, url) success {

                    val oldFolder = File(STMSession.sharedSession!!.filing.webPath(title))

                    STMFunctions.deleteRecursive(oldFolder)

                    File(it).renameTo(oldFolder)

                    if (needLoad){

                        runOnUiThread{

                            webView?.loadUrl("file:///$webPath")

                        }

                    }

                } fail {

                    STMFunctions.debugLog("WebViewActivity",it.localizedMessage)

                    if (needLoad){

                        err = it.localizedMessage

                    }


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

            val photoMapParameters = this.photoMapParameters

            this.photoMapParameters = null

            if (nativePath.isEmpty()){

                return webInterface!!.javascriptCallback("canceled", photoMapParameters)

            }

            val photoEntityName = photoMapParameters!!["entityName"] as String

            val file:Bitmap? = if (nativePath.startsWith("content:/")){

                BitmapFactory.decodeStream(MyApplication.appContext!!.contentResolver.openInputStream(Uri.parse(nativePath)))

            } else {

                BitmapFactory.decodeFile(filePath)

            }


            val attributes = HashMap(STMCorePhotosController.sharedInstance!!.newPhotoObject(photoEntityName, file!!))

            val photoData = photoMapParameters["data"] as Map<*,*>

            attributes.putAll(photoData)

            STMSession.sharedSession!!.persistenceDelegate.merge(photoEntityName, attributes.toMap(), null).then {

                val callback = photoMapParameters["callback"] as String
                webInterface!!.javascriptCallback(arrayListOf(it), photoMapParameters, callback)

                STMCorePhotosController.sharedInstance!!.uploadPhotoEntityName(photoEntityName, attributes, file)

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

        updateHandler.removeCallbacksAndMessages(null)
        
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

    private fun initUpdater(){

        val appUpdater = AppUpdater(this)
        appUpdater.setUpdateFrom(UpdateFrom.GOOGLE_PLAY)
        appUpdater.setButtonDoNotShowAgain("")

        val runnableCode = object : Runnable {
            override fun run() {

                appUpdater.start()
                updateHandler.postDelayed(this, 1000 * 60 * 60 * 24)
            }
        }
        updateHandler.post(runnableCode)

    }

    fun loadFromManifest(manifest:String, title:String, url:String): Promise<String, Exception> {

        return task {

            val webPath = STMSession.sharedSession!!.filing.tempWebPath(title)

            val (_, response, result) = Fuel.get(manifest).responseJson()

            val etag = response.headers["ETag"]?.first()

            val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

            val savedTag = prefStore?.getString("${title}ETag", null)

            if (etag != savedTag && etag != null){

                STMFunctions.debugLog("STMCoreAuthController","update available")

                val filePaths = filePathsToLoadFromAppManifest(result.get().content)

                for (file in filePaths){

                    val (_,_, res) =  Fuel.download("$url/$file").destination { _, _ ->

                        File.createTempFile("temp", ".tmp")

                    }.response()

                    val path = "$webPath/$file"

                    File(path.removeSuffix("/" + path.split("/").last())).mkdirs()

                    if (res.component1()!!.isNotEmpty()){

                        STMFunctions.debugLog("WebViewActivity","finished downloading $file saved to $webPath/$file")

                        File("$webPath/$file").writeBytes(res.component1()!!)

                    }else{

                        STMFunctions.debugLog("WebViewActivity","did not received any bytes of file $file")

                    }

                }

                prefStore?.edit()?.putString("${title}ETag", etag)?.apply()

                return@task webPath

            }

            throw Exception("no update")

        }


    }

}