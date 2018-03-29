package com.sistemium.sissales.activities

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.sistemium.sissales.R
import java.util.*

/**
 * Created by edgarjanvuicik on 23/03/2018.
 */
class ProfileAdapter(val context: Context, private val tabs:ArrayList<Map<*,*>>) : BaseAdapter() {

    override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {

        val tab = tabs[p0]

        var convertView = p1

        if (p1 == null) {
            val layoutInflater = LayoutInflater.from(context)
            convertView = layoutInflater.inflate(R.layout.linearlayout_tabs, p1)
        }

        val imageView = convertView!!.findViewById(R.id.imageview_cover) as ImageView
        val nameTextView = convertView.findViewById(R.id.textview_tab_name) as TextView

        var imageName = tab["imageName"] as? String

        if (imageName != null){

            imageName = imageName.replace("-", "_").replace(" ", "_").toLowerCase().removeSuffix(".png")

            if (imageName[0].isDigit()){

                imageName = "drawable_$imageName"

            }

            val resID = context.resources.getIdentifier(imageName, "drawable", context.packageName)

            imageView.setImageResource(resID)

            nameTextView.text = tab["title"] as? String

        }

        return convertView
    }

    override fun getItem(p0: Int): Any {
        return Object()
    }

    override fun getItemId(p0: Int): Long {
        return 0
    }

    override fun getCount(): Int {
        return tabs.size
    }

}