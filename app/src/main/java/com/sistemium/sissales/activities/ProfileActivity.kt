package com.sistemium.sissales.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.appcompat.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.sistemium.sissales.R
import com.sistemium.sissales.activityController.ProfileActivityController
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import kotlinx.android.synthetic.main.activity_profile.*
import com.github.javiersantos.appupdater.AppUpdater
import com.github.javiersantos.appupdater.enums.UpdateFrom
import com.sistemium.sissales.BuildConfig
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.classes.entitycontrollers.STMEntityController


class ProfileActivity : AppCompatActivity() {

    companion object {

        var profileActivityController: ProfileActivityController? = null

    }

    var progressBar: ProgressBar? = null

    var progressInfo: TextView? = null

    var currentTab: Map<*, *>? = null

    val tabs = arrayListOf<Map<*, *>>()

    var profileAdapter: ProfileAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        STMFunctions.memoryFix()

        super.onCreate(savedInstanceState)
        profileActivityController = ProfileActivityController(this)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)

        progressBar = findViewById(R.id.progressBar)

        progressBar!!.visibility = View.INVISIBLE

        progressInfo = findViewById(R.id.progress_info)

        val profileName: TextView = findViewById(R.id.profileName)

        profileName.text = "${STMCoreAuthController.userName}"

        val phoneNumber: TextView = findViewById(R.id.phoneNumber)

        phoneNumber.text = STMCoreAuthController.phoneNumber ?: ""

        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)

        toolbarTitle.text = STMCoreAuthController.userAgent

        val gridView = findViewById<GridView>(R.id.gridView)

        for (tab in STMCoreAuthController.stcTabs ?: arrayListOf<Map<*,*>>()) {

            if (tab is Map<*, *> && tab["name"] == "STMWKWebView") {

                tabs.add(tab)

            }

        }

        val logout:ImageButton = findViewById(R.id.logout)

        logout.setOnClickListener{

            runOnUiThread {

                val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
                } else {
                    AlertDialog.Builder(this)
                }
                builder.setTitle(this.resources.getString(R.string.logout))
                        .setMessage(this.resources.getString(R.string.you_sure))
                        .setPositiveButton(android.R.string.ok) { _, _ ->

                            runOnUiThread {

                                startActivity(Intent(this@ProfileActivity, AuthActivity::class.java))

                                STMCoreAuthController.logout()

                                finish()

                            }

                        }
                        .setNegativeButton(android.R.string.cancel) { _, _ -> }
                        .show()

            }

        }

        profileAdapter = ProfileAdapter(this, tabs)

        gridView.adapter = profileAdapter

        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            currentTab = tabs[position]

            openWeb()

        }

    }

    var webOpened = false

    fun openWeb(){

        webOpened = true

        if (currentTab == null){
            currentTab = tabs.first()
        }

        val intent = Intent(this@ProfileActivity, WebViewActivity::class.java)

        var url = currentTab!!["url"] as? String

        val manifest = currentTab!!["appManifestURI"] as? String

        if (url == null && manifest != null){

            url = manifest.replace(manifest.split("/").last(), "")

        }

        if (url?.endsWith("/") != true){
            url += "/"
        }

        //debug
//            url = url?.replace("http://lamac.local:3000", "http://10.0.1.5:3000")
//            url = url?.replace("http://lamac.local:3000", "http://192.168.0.103:3000")
//            if (manifest == null){
//                manifest = "$url/app.manifest"
//            }
        //debug

        intent.putExtra("url", url)
        intent.putExtra("manifest", manifest)
        intent.putExtra("title", currentTab!!["title"] as String)
        startActivity(intent)
    }

    override fun onBackPressed() {
        this.moveTaskToBack(true)
    }

}
