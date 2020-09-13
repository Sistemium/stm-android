package com.sistemium.sissales.activityController

import android.annotation.SuppressLint
import android.view.View
import com.sistemium.sissales.R
import com.sistemium.sissales.activities.PlaceholderFragment
import com.sistemium.sissales.activities.ProfileActivity
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.classes.entitycontrollers.STMEntityController
import com.sistemium.sissales.base.session.STMSession
import com.sistemium.sissales.base.session.STMSyncer
import kotlinx.android.synthetic.main.content_profile.*

/**
 * Created by edgarjanvuicik on 20/03/2018.
 */
class ProfileActivityController(private var placeholder: PlaceholderFragment) {

    companion object {

        var got = 0

    }

    fun setMaxProgress(max: Int) {

        placeholder.activity!!.runOnUiThread {

            placeholder.progressBar!!.max = max

            placeholder.progressBar!!.progress = 0

            if (max > 0) {

                placeholder.progressBar!!.visibility = View.VISIBLE

            }

        }

    }

    fun addProgress(progress: Int) {

        placeholder.activity!!.runOnUiThread {

            placeholder.activity!!.progressBar!!.progress += progress

            if (placeholder.activity!!.progressBar!!.progress >= placeholder.activity!!.progressBar!!.max) {

                placeholder.activity!!.progressBar!!.visibility = View.INVISIBLE

            }

        }

    }

    fun setProgressInfo(info: Int) {

        placeholder.activity!!.runOnUiThread {

            got += info

            val text = "${MyApplication.appContext!!.getString(R.string.receive)} $got ${MyApplication.appContext!!.getString(R.string.objects)}"

            placeholder.progressInfo?.text = text

            if (info == -1) {

                got = 0

                placeholder.progressInfo?.text = ""

            }

        }

    }

}