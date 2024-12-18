package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.webkit.*
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.github.kittinunf.fuel.Fuel
import com.sistemium.sissales.BuildConfig
import com.sistemium.sissales.R
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMBarCodeScanner
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.classes.entitycontrollers.STMCorePhotosController
import com.sistemium.sissales.base.helper.logger.STMLogger
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.webInterface.WebAppInterface
import io.flutter.FlutterInjector
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.task
import nl.komponents.kovenant.then
import java.io.*


@SuppressLint("SetJavaScriptEnabled")
class WebViewActivity : Activity() {

    companion object {

        var webInterface: WebAppInterface? = null

    }

    var webView: WebView? = null
    var filePath: String? = null
    var photoMapParameters: Map<*, *>? = null
    private var mUMA: ValueCallback<Array<Uri>>? = null
    private var err: String? = null
    private val updateHandler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {

        //https://stackoverflow.com/questions/51843546/android-pie-9-0-webview-in-multi-process
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (packageName != Application.getProcessName()) {
                WebView.setDataDirectorySuffix(Application.getProcessName())
            }
        }

        STMFunctions.memoryFix()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        WebView.setWebContentsDebuggingEnabled(true)
        if (BuildConfig.APPLICATION_ID.contains(".warehouse")) {
            window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }

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
                if (consoleMessage!!.message().startsWith("Uncaught Error")) {
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

            //https://stackoverflow.com/questions/39979950/webviewclient-not-calling-shouldoverrideurlloading
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                if (url?.contains("access-token=") == true && manifest == null) {

                    STMCoreAuthController.accessToken = url.toString().split("access-token=").last()

                    webView?.destroy()

                    STMCoreAuthController.logIn()

                }
                if (url?.startsWith("tel:") == true) {
                    val intent = Intent(Intent.ACTION_DIAL,
                            Uri.parse(url.toString()))
                    startActivity(intent)
                    return true
                }
                return false
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                if (request?.url?.toString()?.contains("access-token=") == true && manifest == null) {

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

        if (STMCoreAuthController.isDemo) {
            val loader = FlutterInjector.instance().flutterLoader()
            var key = loader.getLookupKeyForAsset("assets/demo/${STMCoreAuthController.configuration.lowercase()}/${title!!}/localHTML")

            val assetManager = MyApplication.appContext!!.assets

            key = key.split('/').map { return@map Uri.encode(it).toString() }.joinToString("/")

            assetManager.copyAssetFolder(key, STMCoreSessionFiler.sharedSession!!.webPath(title!!))
            task {
                runOnUiThread {
                    val webPath = "file:///" + STMCoreSessionFiler.sharedSession!!.webPath(title!!) + "/index.html"
                    webView?.loadUrl(webPath)
                }
            }

            return
        }

        if (manifest == null) {

            netDirectLoad(url!!)

            return

        } else {

            initUpdater()

            STMFunctions.initPermissions(this)

        }

        task {

            runOnUiThread {

                val webPath = STMCoreSessionFiler.sharedSession!!.webPath(title!!) + "/index.html"

                val webFolder = File(webPath)

                var needLoad = false

                if (webFolder.exists()) {

                    webView?.loadUrl("file:///$webPath")

                } else {

                    needLoad = true

                }

                loadFromManifest(manifest!!, title, url!!) success {

                    val oldFolder = File(STMCoreSessionFiler.sharedSession!!.webPath(title))

                    STMFunctions.deleteRecursive(oldFolder)

                    File(it).renameTo(oldFolder)

                    if (needLoad) {

                        runOnUiThread {

                            webView?.loadUrl("file:///$webPath")

                        }

                    }

                } fail {

                    STMFunctions.debugLog("WebViewActivity", it.localizedMessage)

                    if (needLoad) {

                        err = it.localizedMessage

                    }

                }

            }

        }

    }

    private fun AssetManager.copyAssetFolder(srcName: String, dstName: String): Boolean {
        return try {
            var result = true
            val fileList = this.list(srcName) ?: return false
            if (fileList.isEmpty()) {
                result = copyAssetFile(srcName, dstName)
            } else {
                val file = File(dstName)
                result = file.mkdirs()
                for (filename in fileList) {
                    result = result and copyAssetFolder(
                            srcName + File.separator.toString() + filename,
                            dstName + File.separator.toString() + Uri.decode(filename)
                    )
                }
            }
            result
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun AssetManager.copyAssetFile(srcName: String, dstName: String): Boolean {
        return try {
            val inStream = this.open(srcName)
            val outFile = File(dstName)
            val out: OutputStream = FileOutputStream(outFile)
            val buffer = ByteArray(1024)
            var read: Int
            while (inStream.read(buffer).also { read = it } != -1) {
                out.write(buffer, 0, read)
            }
            inStream.close()
            out.close()
            true
        } catch (e: IOException) {
            e.printStackTrace()
            false
        }
    }

    private fun netDirectLoad(url: String) {

        webView!!.settings!!.cacheMode = WebSettings.LOAD_NO_CACHE
        webView?.settings?.userAgentString = "Mozilla/5.0 (Linux; Android 4.1.1; Galaxy Nexus Build/JRO03C) AppleWebKit/535.19 (KHTML, like Gecko) Chrome/18.0.1025.166 Mobile Safari/535.19"

        val dataDir = this.applicationInfo.dataDir

        val appWebViewDir = File("$dataDir/app_webview/")
        STMFunctions.deleteRecursive(appWebViewDir)

        if ((getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager)
                        .activeNetworkInfo?.isConnected == true) {

            webView!!.loadUrl(url, hashMapOf("Pragma" to "no-cache", "Cache-Control" to "no-cache"))

        } else {

            STMFunctions.handleError(this, resources.getString(R.string.no_internet)) { _, _ ->

                netDirectLoad(url)

            }

        }

        return

    }

    override fun onBackPressed() {

        if (err != null) {

            this.goBack()

        } else {

            webView?.evaluateJavascript("window.history.back()") {

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

            if (nativePath.isEmpty()) {

                return webInterface!!.javascriptCallback("canceled", photoMapParameters)

            }

            val photoEntityName = photoMapParameters!!["entityName"] as String

            val file: Bitmap? = if (nativePath.startsWith("content:/")) {

                BitmapFactory.decodeStream(MyApplication.appContext!!.contentResolver.openInputStream(Uri.parse(nativePath)))

            } else {

                BitmapFactory.decodeFile(filePath)

            }


            val attributes = HashMap(STMCorePhotosController.sharedInstance!!.newPhotoObject(photoEntityName, file!!))

            val photoData = photoMapParameters["data"] as Map<*, *>

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

    private fun filePathsToLoadFromAppManifest(manifest: String): List<String> {

        return manifest.split("\n").map {

            return@map it.filter {

                if (it == ' ') {

                    return@filter false

                }

                return@filter true

            }

        }.filter {

            if (it == "" || it.startsWith("#") || it == "favicon.ico" || it == "robots.txt"
                    || it == "CACHEMANIFEST" || it == "CACHE:" || it == "NETWORK:" || it == "*") {

                return@filter false

            }

            return@filter true

        }

    }

    private fun initUpdater() {

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

    private fun loadFromManifest(manifest: String, title: String, url: String): Promise<String, Exception> {
        return task {
            val files = hashMapOf<String, ByteArray>()
            val webPath = STMCoreSessionFiler.sharedSession!!.tempWebPath(title)
            val (_, response, _) = Fuel.get(manifest).response()
            val etag = response.headers["ETag"]?.first()
            val prefStore = MyApplication.appContext?.getSharedPreferences("Sistemium", Context.MODE_PRIVATE)
            val savedTag = prefStore?.getString("${title}ETag", null)
            if (etag != savedTag && etag != null) {
                STMFunctions.debugLog("STMCoreAuthController", "update available")
                val jsonString = String(response.data)
                val filePaths = filePathsToLoadFromAppManifest(jsonString)
                for (file in filePaths) {
                    val (_, _, res) = Fuel.download("$url$file").destination { _, _ ->
                        File.createTempFile("temp-${file.replace(".", "").replace("/", "")}", ".tmp")
                    }.response()
                    val path = "$webPath/$file"
                    if (res.component1()?.isNotEmpty() == true) {
                        files[path] = res.component1()!!
                    } else {
                        throw Exception("did not received any bytes of file $file, aborting load")
                    }
                }
                for ((path, file) in files) {
                    File(path.removeSuffix("/" + path.split("/").last())).mkdirs()
                    STMFunctions.debugLog("WebViewActivity", "finished downloading file saved to $path")
                    File(path).writeBytes(file)
                }
                prefStore?.edit()?.putString("${title}ETag", etag)?.apply()
                return@task webPath
            }
            throw Exception("no update")
        }
    }
}