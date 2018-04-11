package com.sistemium.sissales.activities

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.AdapterView
import android.widget.GridView
import android.widget.ProgressBar
import android.widget.TextView
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

        val profileAdapter = ProfileAdapter(this, tabs)

        gridView.adapter = profileAdapter

        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            currentTab = tabs[position]

            val intent = Intent(this@ProfileActivity, WebViewActivity::class.java)

            var url = currentTab!!["url"] as? String

            var manifest = currentTab!!["appManifestURI"] as? String


            //debug
            url = url?.replace("http://lamac.local:3000", "http://10.0.1.5:3000")
            manifest = manifest?.replace("http://lamac.local:3000", "http://10.0.1.5:3000")
            manifest = "$url/app.manifest"

            if (manifest != null) {

                intent.putExtra("manifest", manifest)

            }

            intent.putExtra("url", url)
            startActivity(intent)
            profileAdapter.notifyDataSetChanged()

        }

    }

    override fun onBackPressed() {
        this.moveTaskToBack(true)
    }

}
