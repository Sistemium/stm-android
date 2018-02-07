package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.webkit.WebView
import com.sistemium.sissales.R
import com.sistemium.sissales.WebInterface.WebAppInterface
import com.sistemium.sissales.base.STMConstants
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.enums.STMStorageType
import com.sistemium.sissales.interfaces.STMFullStackPersisting
import com.sistemium.sissales.model.STMSQLiteDatabaseAdapter
import com.sistemium.sissales.model.STMModeller
import com.sistemium.sissales.persisting.*

@SuppressLint("SetJavaScriptEnabled")
class WebViewActivity : Activity() {

    var webView: WebView? = null

    var persistenceDelegate: STMFullStackPersisting? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.webview)

        //TODO modelName needs to be received from settings

        val modelName = "iSisSales"

        val filing = STMCoreSessionFiler(this)

        val modeler = STMModeller(filing.bundledModelJSON(modelName))

        val dbPath = filing.persistencePath(STMConstants.SQL_LITE_PATH, modelName)

        val adapter = STMSQLiteDatabaseAdapter(modeler, dbPath)

        val runner = STMPersisterRunner(hashMapOf(STMStorageType.STMStorageTypeSQLiteDatabase to adapter))

        val persister = STMPersister(runner)

        val entityNameInterceptor = STMPersistingInterceptorUniqueProperty()
        entityNameInterceptor.entityName = STMConstants.STM_ENTITY_NAME
        entityNameInterceptor.propertyName = "name"

        persister.beforeMergeEntityName(entityNameInterceptor.entityName!!, entityNameInterceptor)

        val settingsInterceptor = STMCoreSettingsController()

        persister.beforeMergeEntityName(STMConstants.STM_SETTING_NAME, settingsInterceptor)

        val recordStatusInterceptor = STMRecordStatusController()

        persister.beforeMergeEntityName(STMConstants.STM_RECORDSTATUS_NAME, recordStatusInterceptor)

        persistenceDelegate = persister

        modeler.persistanceDelegate = persistenceDelegate

        webView = findViewById(R.id.webView1)
        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true

        val webInterface = WebAppInterface(this)

        webView?.addJavascriptInterface(webInterface, "stmAndroid")

        webView?.loadUrl("http://10.0.1.5:3000")


    }
}