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
import devliving.online.securedpreferencestore.DefaultRecoveryHandler
import devliving.online.securedpreferencestore.SecuredPreferenceStore

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        SecuredPreferenceStore.init(applicationContext, DefaultRecoveryHandler())
        val prefStore = SecuredPreferenceStore.getSharedInstance()
        val accessToken = prefStore.getString("accessToken", null)

        if (accessToken != null){

            val myIntent = Intent(this@AuthActivity, WebViewActivity::class.java)

            myIntent.putExtra("accessToken", accessToken)

            val options = ActivityOptions.makeCustomAnimation(this, R.anim.abc_fade_in, R.anim.abc_fade_out)

            this@AuthActivity.startActivity(myIntent, options.toBundle())

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

            Fuel.get("https://api.sistemium.com/pha/auth", listOf("mobileNumber" to phoneNumberEdit.text))
                    .responseJson { _, _, result ->

                        when (result) {
                            is Result.Failure -> {

                                val error:Error? = result.getAs()

                                handleError(error)

                            }
                            is Result.Success -> {

                                val data = result.get().obj()

                                val myIntent = Intent(this@AuthActivity, CodeConfirmActivity::class.java)
                                myIntent.putExtra("ID", data.get("ID") as String)
                                myIntent.putExtra("mobileNumber", phoneNumberEdit.text.toString())

                                val options = ActivityOptions.makeCustomAnimation(this, R.anim.abc_fade_in, R.anim.abc_fade_out)

                                this@AuthActivity.startActivity(myIntent, options.toBundle())

                            }
                        }

                    }

        }

        clickButton.setOnClickListener( onClickListener )
    }

    private fun handleError(error: Error?){

        if (error is Error){

            Log.d("ERROR", error.toString())

        }

        val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert)
        } else {
            AlertDialog.Builder(this)
        }
        builder.setTitle("Error")
                .setMessage("Wrong phone number")
                .setPositiveButton(android.R.string.ok, { _, _ -> })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()

    }

}
