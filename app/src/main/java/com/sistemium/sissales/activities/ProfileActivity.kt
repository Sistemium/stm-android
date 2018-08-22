package com.sistemium.sissales.activities

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.*
import com.sistemium.sissales.R
import com.sistemium.sissales.activityController.ProfileActivityController
import com.sistemium.sissales.base.session.STMCoreAuthController
import kotlinx.android.synthetic.main.activity_profile.*

class ProfileActivity : AppCompatActivity() {

    companion object {

        var profileActivityController: ProfileActivityController? = null

    }

    var progressBar: ProgressBar? = null

    var progressInfo: TextView? = null

    var currentTab: Map<*, *>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileActivityController = ProfileActivityController(this)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)

        initPermissions()

        progressBar = findViewById(R.id.progressBar)

        progressBar!!.visibility = View.INVISIBLE

        progressInfo = findViewById(R.id.progress_info)

        val profileName: TextView = findViewById(R.id.profileName)

        profileName.text = "${STMCoreAuthController.userName}"

        val phoneNumber: TextView = findViewById(R.id.phoneNumber)

        phoneNumber.text = "${STMCoreAuthController.phoneNumber}"

        val gridView = findViewById<GridView>(R.id.gridView)

        val tabs = arrayListOf<Map<*, *>>()

        for (tab in STMCoreAuthController.stcTabs!!) {

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

        val profileAdapter = ProfileAdapter(this, tabs)

        gridView.adapter = profileAdapter

        gridView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
            currentTab = tabs[position]

            val intent = Intent(this@ProfileActivity, WebViewActivity::class.java)

            var url = currentTab!!["url"] as? String

            val manifest = currentTab!!["appManifestURI"] as? String

            if (url == null && manifest != null){

                url = manifest.replace(manifest.split("/").last(), "")

            }

//            //debug
//            url = url?.replace("http://lamac.local:3000", "http://10.0.1.5:3000")

            intent.putExtra("url", url)
            startActivity(intent)
            profileAdapter.notifyDataSetChanged()

        }

    }

    override fun onBackPressed() {
        this.moveTaskToBack(true)
    }

    private fun initPermissions(){

        val permissions = arrayListOf<String>()

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            permissions.add(Manifest.permission.CAMERA)

        }

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        }

        if (permissions.isNotEmpty()){

            ActivityCompat.requestPermissions(this,
                    permissions.toTypedArray(),
                    0)

        }

    }

}