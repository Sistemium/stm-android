package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.webkit.*
import android.webkit.WebView
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePhotosController
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.webInterface.WebAppInterface
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import java.io.File
import com.sistemium.sissales.R

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

        webView?.settings?.databaseEnabled = true
        webView?.settings?.domStorageEnabled = true

        webView?.settings?.allowFileAccess = true
        webView?.settings?.allowContentAccess = true
        webView?.settings?.allowUniversalAccessFromFileURLs = true
        webView?.settings?.allowFileAccessFromFileURLs = true

        webInterface = WebAppInterface(this)

        webView?.addJavascriptInterface(webInterface!!, "stmAndroid")
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

        webView?.webViewClient = object : WebViewClient() {

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                STMFunctions.debugLog("CHROME", error.toString())
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (request?.url?.toString()?.contains("access-token=") == true && manifest == null){

                    STMCoreAuthController.accessToken = request.url.toString().split("access-token=").last()

                    webView?.destroy()

                    STMCoreAuthController.logIn()

                }
                if (request?.url?.toString()?.startsWith("tel:") == true) {
                    val intent = Intent(Intent.ACTION_DIAL,
                            Uri.parse(request.url.toString()))
                    startActivity(intent)
                    return true
                }
                return false
            }

        }

        if(manifest == null) {

            netDirectLoad(url!!)

            return

        } else {

            initUpdater()

            STMFunctions.initPermissions(this)

        }

        task {

            runOnUiThread {

                val webPath = STMCoreSessionFiler.sharedSession!!.webPath(title!!)+"/index.html"

                val webFolder = File(webPath)

                var needLoad = false

                if (webFolder.exists()){

                    webView?.loadUrl("file:///$webPath")

                } else {

                    needLoad = true

                }

                loadFromManifest(manifest!!, title, url!!) success {

                    val oldFolder = File(STMCoreSessionFiler.sharedSession!!.webPath(title))

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

    private fun netDirectLoad(url:String){

        webView!!.settings!!.setAppCacheEnabled(false)
        webView!!.settings!!.cacheMode = WebSettings.LOAD_NO_CACHE
        webView?.settings?.userAgentString = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"

        val dataDir = this.applicationInfo.dataDir

        val appWebViewDir = File("$dataDir/app_webview/")
        STMFunctions.deleteRecursive(appWebViewDir)

        if ((getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                        .activeNetworkInfo?.isConnected == true){

            webView!!.loadUrl(url, hashMapOf("Pragma" to "no-cache", "Cache-Control" to "no-cache"))

        } else {

            STMFunctions.handleError(this, resources.getString(R.string.no_internet)){ _,_ ->

                netDirectLoad(url)

            }

        }

        return

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
                        results = arrayOf(Uri.parse(dataString))
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

                STMFunctions.debugLog("onActivityResult", "onActivityResult success")

                val callback = photoMapParameters["callback"] as String
                webInterface!!.javascriptCallback(arrayListOf(it), photoMapParameters, callback)

                STMCorePhotosController.sharedInstance!!.uploadPhotoEntityName(photoEntityName, attributes, file)

            }.fail {

                STMFunctions.debugLog("onActivityResult", "onActivityResult failed")

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

    private fun loadFromManifest(manifest:String, title:String, url:String): Promise<String, Exception> {

        return task {

            val files = hashMapOf<String, ByteArray>()

            val webPath = STMCoreSessionFiler.sharedSession!!.tempWebPath(title)

            val (_, response, result) = Fuel.get(manifest).responseJson()

            val etag = response.headers["ETag"]?.first()

            val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)

            val savedTag = prefStore?.getString("${title}ETag", null)

            if (etag != savedTag && etag != null){

                STMFunctions.debugLog("STMCoreAuthController","update available")

                val filePaths = filePathsToLoadFromAppManifest(result.get().content)

                for (file in filePaths){

                    val (_,_, res) =  Fuel.download("$url/$file").destination { _, _ ->

                        File.createTempFile("temp-${file.replace(".","").replace("/","")}", ".tmp")

                    }.response()

                    val path = "$webPath/$file"

                    if (res.component1()?.isNotEmpty() == true){

                        files[path] = res.component1()!!

                    } else {

                        throw Exception("did not received any bytes of file $file, aborting load")

                    }

                }

                for ((path, file) in files){

                    File(path.removeSuffix("/" + path.split("/").last())).mkdirs()

                    STMFunctions.debugLog("WebViewActivity","finished downloading file saved to $path")

                    File(path).writeBytes(file)

                }

                prefStore?.edit()?.putString("${title}ETag", etag)?.apply()

                return@task webPath

            }

            throw Exception("no update")

        }


    }

}