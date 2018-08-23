package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import com.sistemium.sissales.R
import com.sistemium.sissales.webInterface.WebAppInterface
import com.sistemium.sissales.base.STMFunctions
import nl.komponents.kovenant.task
import android.content.Intent
import android.os.Environment
import android.provider.MediaStore
import java.io.File
import java.io.IOException
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

    private var err:String?  = null

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
                        err = error.toString()
                        STMFunctions.debugLog("CHROME", error.toString())
                    }

                    override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                        return false
                    }

                }

                webView?.webChromeClient = object : WebChromeClient() {

                    override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                        if (consoleMessage!!.message().startsWith("Uncaught Error")){
                            err = consoleMessage.message()
                        }
                        return super.onConsoleMessage(consoleMessage)
                    }

                }

                webView?.loadUrl(url)

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