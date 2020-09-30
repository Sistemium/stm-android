package com.sistemium.sissales.activities

import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.sistemium.sissales.R
import com.sistemium.sissales.activityController.ProfileActivityController
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController
import kotlinx.android.synthetic.main.activity_profile.*

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment(private val tab:Map<*, *>) : Fragment() {

    companion object {

        var profileActivityController: ProfileActivityController? = null

    }

    var progressBar: ProgressBar? = null

    var progressInfo: TextView? = null

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
       if (tab["name"] == "STMProfile") {
           val root = inflater.inflate(R.layout.activity_profile, container, false)
           profileActivityController = ProfileActivityController(this)
//        activity?.setContentView(R.layout.activity_profile)
//        setSupportActionBar(toolbar)

           progressBar = root.findViewById(R.id.progressBar)

           progressBar!!.visibility = View.INVISIBLE

           progressInfo = root.findViewById(R.id.progress_info)

           val profileName: TextView = root.findViewById(R.id.profileName)

           profileName.text = "${STMCoreAuthController.userName}"

           val phoneNumber: TextView = root.findViewById(R.id.phoneNumber)

           phoneNumber.text = STMCoreAuthController.phoneNumber ?: ""

           val toolbarTitle: TextView = root.findViewById(R.id.toolbar_title)
           toolbarTitle.text = STMCoreAuthController.userAgent

           val logout: ImageButton = root.findViewById(R.id.logout)

           logout.setOnClickListener{

               activity?.runOnUiThread {

                   val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                       AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
                   } else {
                       AlertDialog.Builder(activity)
                   }
                   builder.setTitle(this.resources.getString(R.string.logout))
                           .setMessage(this.resources.getString(R.string.you_sure))
                           .setPositiveButton(android.R.string.ok) { _, _ ->

                               activity?.runOnUiThread {

                                   startActivity(Intent(activity, AuthActivity::class.java))

                                   STMCoreAuthController.logout()

                                   activity?.finish()

                               }

                           }
                           .setNegativeButton(android.R.string.cancel) { _, _ -> }
                           .show()

               }

           }
           return root;
        } else{
           val root = inflater.inflate(R.layout.activity_profile, container, false)
           return root;
        }

    }
}