package com.sistemium.sissales.activities

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.sistemium.sissales.R

/**
 * A placeholder fragment containing a simple view.
 */
class PlaceholderFragment(private val tab:Map<*, *>) : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
    }

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        return if (tab["name"] == "STMProfile") {
            inflater.inflate(R.layout.activity_profile, container, false)
        } else{
            inflater.inflate(R.layout.activity_profile, container, false)
        }
    }
}