package com.sistemium.sissales.activities

import android.app.ActivityOptions
import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_code_confirm.*
import android.content.Intent
import android.os.Build
import android.util.Log
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.android.extension.responseJson
import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getAs
import com.sistemium.sissales.R
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController
import devliving.online.securedpreferencestore.SecuredPreferenceStore

class CodeConfirmActivity : AppCompatActivity() {

    private var mobileNumber:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_confirm)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val intent = intent

        val id = intent.getStringExtra("ID")
        mobileNumber = intent.getStringExtra("mobileNumber")

        val sendButton: Button = findViewById(R.id.button)
        val smsCodeEdit: EditText = findViewById(R.id.editText)

        smsCodeEdit.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->

            if (event.action !=KeyEvent.ACTION_DOWN){

                return@OnKeyListener true

            }

            if(keyCode == KeyEvent.KEYCODE_ENTER){

                sendButton.performClick()

                return@OnKeyListener true

            }

            false
        })


        val onClickListener = View.OnClickListener {

            val accessToken = STMCoreAuthController.requestAccessToken(id, smsCodeEdit.text.toString())

            if (accessToken != null){

                if(STMCoreAuthController.logIn()){

                    finish()

                }

            }

        }

        sendButton.setOnClickListener( onClickListener )

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.getItemId()) {
            android.R.id.home -> {
                val intent = Intent(this@CodeConfirmActivity, AuthActivity::class.java)
                intent.putExtra("mobileNumber", mobileNumber)
                finish()
                startActivity(intent)
                return true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

}
