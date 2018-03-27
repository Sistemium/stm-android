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

    var progressBar:ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileActivityController = ProfileActivityController(this)
        setContentView(R.layout.activity_profile)
        setSupportActionBar(toolbar)

        progressBar = findViewById(R.id.progressBar)

        progressBar!!.visibility = View.INVISIBLE

        val profileName:TextView = findViewById(R.id.profileName)

        profileName.text = "${STMCoreAuthController.userName}"

        val phoneNumber:TextView = findViewById(R.id.phoneNumber)

        phoneNumber.text = "${STMCoreAuthController.phoneNumber}"

        val gridView = findViewById<GridView>(R.id.gridView)

        val tabs = arrayListOf<Map<*,*>>()

        for (tab in STMCoreAuthController.stcTabs!!){

            if (tab is Map<*,*> && tab["name"] == "STMWKWebView"){

                tabs.add(tab)

            }

        }

        val booksAdapter = ProfileAdapter(this, tabs)
        gridView.adapter = booksAdapter

        gridView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val tab = tabs[position]

            val intent = Intent(this@ProfileActivity, WebViewActivity::class.java)
            intent.putExtra("url",tab["url"] as String)
            startActivity(intent)
            booksAdapter.notifyDataSetChanged()
            finish()
        }

    }

    override fun onBackPressed() {
        this.moveTaskToBack(true)
    }

}
