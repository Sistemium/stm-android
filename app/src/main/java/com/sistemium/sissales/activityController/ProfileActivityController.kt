package com.sistemium.sissales.activityController

import android.view.View
import com.sistemium.sissales.R
import com.sistemium.sissales.activities.ProfileActivity
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.calsses.entitycontrollers.STMEntityController
import kotlinx.android.synthetic.main.content_profile.*

/**
 * Created by edgarjanvuicik on 20/03/2018.
 */
class ProfileActivityController(private var activity: ProfileActivity) {

    companion object {

        var got = 0

    }

    fun setMaxProgress(max: Int) {

        activity.runOnUiThread {

            activity.gridView.visibility = if (STMEntityController.downloadableEntityReady()) View.VISIBLE else View.INVISIBLE

            activity.progressBar!!.max = max

            activity.progressBar!!.progress = 0

            if (max > 0) {

                activity.progressBar!!.visibility = View.VISIBLE

            }

        }

    }

    fun addProgress(progress: Int) {

        activity.runOnUiThread {

            activity.progressBar!!.progress += progress

            if (activity.progressBar!!.progress >= activity.progressBar!!.max) {

                activity.progressBar!!.visibility = View.INVISIBLE

            }

        }

    }

    fun setProgressInfo(info: Int) {

        activity.runOnUiThread {

            got += info

            val text = "${MyApplication.appContext!!.getString(R.string.receive)} $got ${MyApplication.appContext!!.getString(R.string.objects)}"

            activity.progressInfo?.text = text

            if (info == -1) {

                activity.gridView.visibility = if (STMEntityController.downloadableEntityReady()) View.VISIBLE else View.INVISIBLE

                got = 0

                activity.progressInfo?.text = ""

            }

        }

    }

}