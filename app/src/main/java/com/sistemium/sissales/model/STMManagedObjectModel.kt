package com.sistemium.sissales.model

import android.util.Log
import com.google.gson.Gson

/**
 * Created by edgarjanvuicik on 09/11/2017.
 */

class STMManagedObjectModel(model: String){

    private val gson = Gson()

    init {

        val mapModel = gson.fromJson(model, Map::class.java)

        Log.d("DEBUG", "")

    }


}
