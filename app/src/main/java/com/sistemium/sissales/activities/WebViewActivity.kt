package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.view.View
import android.webkit.*
import android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
import com.sistemium.sissales.R
import com.sistemium.sissales.webInterface.WebAppInterface
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.result.Result
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.R.id.webView1
import com.sistemium.sissales.base.STMFunctions
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import nl.komponents.kovenant.task
import com.sistemium.sissales.R.layout.webview
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException
import android.os.Environment.DIRECTORY_PICTURES
import android.os.Environment.getExternalStoragePublicDirectory
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.session.STMCoreSessionManager
import com.sistemium.sissales.enums.STMSocketEvent
import java.text.SimpleDateFormat
import java.util.*


@SuppressLint("SetJavaScriptEnabled")
class WebViewActivity : Activity() {

    companion object {

        var webInterface: WebAppInterface? = null

    }

    var webView: WebView? = null

    private var mUMA:ValueCallback<Array<Uri>>? = null
    private var mCM: String? = null
    private val FCR = 1

    private var error:String?  = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)
        WebView.setWebContentsDebuggingEnabled(true)

        webView = findViewById(R.id.webView1)
        webView?.settings?.javaScriptEnabled = true

        webView?.settings?.domStorageEnabled = true

        webInterface = WebAppInterface(this)

        webView?.addJavascriptInterface(webInterface, "stmAndroid")
        webView?.settings?.mediaPlaybackRequiresUserGesture = false
        webView?.webChromeClient = object : WebChromeClient() {

            override fun onShowFileChooser(webView: WebView?, filePathCallback: ValueCallback<Array<Uri>>?, fileChooserParams: FileChooserParams?): Boolean {

                if (mUMA != null) {
                    mUMA!!.onReceiveValue(null)
                }
                mUMA = filePathCallback
                var takePictureIntent: Intent? = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                if (takePictureIntent!!.resolveActivity(this@WebViewActivity.packageManager) != null) {
                    var photoFile: File? = null
                    try {
                        photoFile = createImageFile()
                        takePictureIntent.putExtra("PhotoPath", mCM)
                    } catch (ex: IOException) {
                        STMFunctions.debugLog("WebViewActivity", "Image file creation failed")
                    }

                    if (photoFile != null) {
                        mCM = "file:" + photoFile.absolutePath
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile))
                    } else {
                        takePictureIntent = null
                    }
                }
                val contentSelectionIntent = Intent(Intent.ACTION_GET_CONTENT)
                contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE)
                contentSelectionIntent.type = "*/*"
                val intentArray: Array<Intent?>
                intentArray = if (takePictureIntent != null) {
                    arrayOf(takePictureIntent)
                } else {
                    arrayOfNulls(0)
                }
                val chooserIntent = Intent(Intent.ACTION_CHOOSER)
                chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent)
                chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser")
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray)
                startActivityForResult(chooserIntent, FCR)

                return true

            }
        }

        val url = intent.getStringExtra("url")

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
                        STMFunctions.debugLog("CHROME", error.toString())
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }

                }

                webView?.webChromeClient = object : WebChromeClient() {

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        if (consoleMessage!!.message().startsWith("Uncaught Error")){
                            error = consoleMessage.message()
                        }
                        return super.onConsoleMessage(consoleMessage)
                    }

                }

                webView?.loadUrl(url)

            }

        }

    }

    override fun onBackPressed() {

        if (error != null){

            this.goBack()

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        var results: Array<Uri>? = null
        if (resultCode === Activity.RESULT_OK) {
            if (requestCode === FCR) {
                if (null == mUMA) {
                    return
                }
                if (data == null || data.data == null) {
                    if (mCM != null) {
                        results = arrayOf(Uri.parse(mCM))
                    }
                } else {
                    val dataString = data.dataString
                    if (dataString != null) {
                        results = arrayOf(Uri.parse(dataString))
                    }
                }
            }
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

    @Throws(IOException::class)
    private fun createImageFile(): File {
        @SuppressLint("SimpleDateFormat") val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "img_" + timeStamp + "_"
        val storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(imageFileName, ".jpg", storageDir)
    }

}