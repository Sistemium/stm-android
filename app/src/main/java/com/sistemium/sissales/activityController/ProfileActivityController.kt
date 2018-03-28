package com.sistemium.sissales.activityController

import android.view.View
import com.sistemium.sissales.activities.ProfileActivity
import kotlinx.android.synthetic.main.content_profile.*

/**
 * Created by edgarjanvuicik on 20/03/2018.
 */
class ProfileActivityController(var activity:ProfileActivity) {

    fun setMaxProgress(max:Int){

        activity.runOnUiThread{

            activity.progressBar!!.max = max

            activity.progressBar!!.progress = 0

            if (max > 0){

                activity.progressBar!!.visibility = View.VISIBLE

            }

        }

    }

    fun addProgress(progress:Int){

        activity.runOnUiThread{

            activity.progressBar!!.progress += progress

            if (activity.progressBar!!.progress >= activity.progressBar!!.max){

                activity.progressBar!!.visibility = View.INVISIBLE

            }

        }

    }

    fun hideGridView(){

        activity.runOnUiThread{

            activity.gridView.visibility = View.INVISIBLE

        }

    }

    fun showGridView(){

        activity.runOnUiThread{

            activity.gridView.visibility = View.VISIBLE

        }

    }

}