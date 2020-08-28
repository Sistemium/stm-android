package com.sistemium.sissales.activities

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.sistemium.sissales.BuildConfig
import com.sistemium.sissales.R
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController


class TabBarControllerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {

        STMFunctions.memoryFix()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        initPermissions()

        val tabs = arrayListOf<Map<*, *>>()

        for (tab in STMCoreAuthController.stcTabs ?: arrayListOf<Map<*,*>>()) {

            if (tab is Map<*, *> && tab["name"] == "STMWKWebView") {

                tabs.add(tab)

            }

        }

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, tabs)



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
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {

            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

        }

        if ( ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        }

        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED && BuildConfig.APPLICATION_ID.contains(".vfs")) {

            permissions.add(Manifest.permission.READ_CONTACTS)

        }

        if (permissions.isNotEmpty()){

            ActivityCompat.requestPermissions(this,
                    permissions.toTypedArray(),
                    0)

        }

    }

}