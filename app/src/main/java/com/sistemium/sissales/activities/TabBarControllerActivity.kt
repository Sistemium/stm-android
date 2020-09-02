package com.sistemium.sissales.activities

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.sistemium.sissales.BuildConfig
import com.sistemium.sissales.R
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController


class TabBarControllerActivity : AppCompatActivity() {

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        STMFunctions.memoryFix()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tabs)

        initPermissions()

        val tabs = arrayListOf<Map<*, *>>()

        for (tab in STMCoreAuthController.stcTabs ?: arrayListOf<Map<*,*>>()) {

            if (tab is Map<*, *>) {

                tabs.add(tab)

            }

        }

        val sectionsPagerAdapter = SectionsPagerAdapter(this, supportFragmentManager, tabs)

        val viewPager: ViewPager = findViewById(R.id.view_pager)
        viewPager.adapter = sectionsPagerAdapter
        viewPager.setOnTouchListener { _, _ ->
            true
        }

        val tabLayout: TabLayout = findViewById(R.id.tabs)
        tabLayout.setupWithViewPager(viewPager)

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