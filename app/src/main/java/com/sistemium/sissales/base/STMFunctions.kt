package com.sistemium.sissales.base

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.gson.GsonBuilder
import com.sistemium.sissales.BuildConfig
import com.sistemium.sissales.R
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.services.LocationService
import java.io.File
import java.math.BigInteger
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by edgarjanvuicik on 14/11/2017.
 */

class STMFunctions {

    companion object {

        val gson = GsonBuilder().serializeNulls().create()

        fun addPrefixToEntityName(entityName: String): String {

            var _entityName = entityName

            if (!entityName.startsWith(STMConstants.ISISTEMIUM_PREFIX)) {
                _entityName = STMConstants.ISISTEMIUM_PREFIX + entityName
            }
            return _entityName

        }

        fun removePrefixFromEntityName(entityName: String): String {

            var _entityName = entityName

            if (entityName.startsWith(STMConstants.ISISTEMIUM_PREFIX)) {
                _entityName = entityName.removePrefix(STMConstants.ISISTEMIUM_PREFIX)
            }
            return _entityName

        }

        fun stringFrom(date: Date): String {

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US)

            sdf.timeZone = TimeZone.getTimeZone("GMT")

            return sdf.format(date)


        }

        fun uuidString(): String {

            return UUID.randomUUID().toString()

        }

        fun md5FromString(string:String): String {

            val md = MessageDigest.getInstance("MD5")
            return BigInteger(1, md.digest(string.toByteArray())).toString(16).padStart(32, '0')

        }

        fun jsonStringFromObject(value: Any): String {

            return gson.toJson(value)

        }

        fun debugLog(name: String, msg: String) {

            val maxLogSize = 1000
            for (i in 0..msg.length / maxLogSize) {
                val start = i * maxLogSize
                var end = (i + 1) * maxLogSize
                end = if (end > msg.length) msg.length else end
                Log.d("${stringFrom(Date())}:$name", msg.substring(start, end))
            }

        }

        fun handleError(activity: Activity?, message: String, callback: (DialogInterface, Int) -> Unit = {_,_ ->}) {

            activity?.runOnUiThread {

                val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
                } else {
                    AlertDialog.Builder(activity)
                }
                builder.setTitle(activity.resources.getString(R.string.error))
                        .setMessage(message)
                        .setPositiveButton(android.R.string.ok, callback)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()

            }

        }


        @SuppressLint("HardwareIds")
        fun deviceUUID(): String {

            return Settings.Secure.getString(MyApplication.appContext!!.contentResolver, Settings.Secure.ANDROID_ID) + Settings.Secure.getString(MyApplication.appContext!!.contentResolver, Settings.Secure.ANDROID_ID)

        }

        fun deleteRecursive(fileOrDirectory: File) {
            if (fileOrDirectory.isDirectory)
                for (child in fileOrDirectory.listFiles())
                    deleteRecursive(child)

            fileOrDirectory.delete()
        }

        //hope fix not working syncer on certain users
        fun memoryFix(){

            if (STMCoreAuthController.rolesResponse != null && STMSession.sharedSession!!.syncer == null) {

                STMSession.sharedSession!!.setupSyncer()

            }

        }

        fun initPermissions(activity: Activity){

            val permissions = arrayListOf<String>()

            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {

                permissions.add(Manifest.permission.CAMERA)

            }

            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }

            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }

            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)

            }

            if ( ContextCompat.checkSelfPermission( activity, Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {

                permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

            }

            if (ContextCompat.checkSelfPermission(activity,
                            Manifest.permission.READ_CONTACTS)
                    != PackageManager.PERMISSION_GRANTED && BuildConfig.APPLICATION_ID.contains(".vfs")) {

                permissions.add(Manifest.permission.READ_CONTACTS)

            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {

                    permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
                }
                if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.BLUETOOTH_ADMIN)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.BLUETOOTH_ADMIN)
                }
                if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.BLUETOOTH)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.BLUETOOTH)
                }
                if (ContextCompat.checkSelfPermission(activity,
                                Manifest.permission.BLUETOOTH_SCAN)
                        != PackageManager.PERMISSION_GRANTED) {
                    permissions.add(Manifest.permission.BLUETOOTH_SCAN)
                }
            }


            if (permissions.isNotEmpty()){

                ActivityCompat.requestPermissions(activity,
                        permissions.toTypedArray(),
                        0)
//                MyApplication.appContext!!.startService(Intent(MyApplication.appContext!!, LocationService::class.java))

            }

        }


    }

}