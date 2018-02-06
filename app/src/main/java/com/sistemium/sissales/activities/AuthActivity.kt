package com.sistemium.sissales.activities

import android.app.AlertDialog
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import kotlinx.android.synthetic.main.activity_auth.*
import android.widget.EditText
import com.github.kittinunf.fuel.*
import com.github.kittinunf.result.*
import android.os.Build
import android.content.Intent
import com.github.kittinunf.fuel.android.extension.responseJson
import android.app.ActivityOptions
import android.util.Log
import android.view.KeyEvent
import com.sistemium.sissales.R
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore
import nl.komponents.kovenant.then

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (STMCoreAuthController.logIn()){

            return finish()

        }

        setContentView(R.layout.activity_auth)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val phoneNumberEdit: EditText = findViewById(R.id.editText)
        val sendButton: Button = findViewById(R.id.button)
        val intent = intent
        val mobileNumber = intent.getStringExtra("mobileNumber")
        phoneNumberEdit.setText(mobileNumber)
        phoneNumberEdit.setSelection(phoneNumberEdit.text.length)

        phoneNumberEdit.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->

            if (event.action !=KeyEvent.ACTION_DOWN){

                return@OnKeyListener true

            }

            if(keyCode == KeyEvent.KEYCODE_ENTER){

                sendButton.performClick()

                return@OnKeyListener true

            }

            false
        })

        sendButton.isEnabled = phoneNumberEdit.text.length == 11

        val textWatcher = object : TextWatcher {

            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

                sendButton.isEnabled = phoneNumberEdit.text.length == 11

            }

        }

        phoneNumberEdit.addTextChangedListener(textWatcher)

        val clickButton:Button = findViewById(R.id.button)
        val onClickListener = View.OnClickListener {

            STMCoreAuthController.requestNewSMSCode(phoneNumberEdit.text.toString()) then {

                val myIntent = Intent(this@AuthActivity, CodeConfirmActivity::class.java)
                myIntent.putExtra("ID", it)
                myIntent.putExtra("mobileNumber", phoneNumberEdit.text.toString())

                val options = ActivityOptions.makeCustomAnimation(this, R.anim.abc_fade_in, R.anim.abc_fade_out)

                this@AuthActivity.startActivity(myIntent, options.toBundle())

            } fail {

                STMFunctions.handleError(this, "Wrong Phone Number")

            }

        }

        clickButton.setOnClickListener( onClickListener )
    }

}
