package com.sistemium.sissales

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

            Fuel.get("https://api.sistemium.com/pha/auth", listOf("ID" to id, "smsCode" to smsCodeEdit.text))
                    .responseJson { _, _, result ->

                        when (result) {
                            is Result.Failure -> {

                                val error:Error? = result.getAs()

                                handleError(error)

                            }
                            is Result.Success -> {

                                val data = result.get().obj()

                                val myIntent = Intent(this@CodeConfirmActivity, WebViewActivity::class.java)
                                myIntent.putExtra("accessToken", data.get("accessToken") as String)

                                val options = ActivityOptions.makeCustomAnimation(this, R.anim.abc_fade_in, R.anim.abc_fade_out)

                                this@CodeConfirmActivity.startActivity(myIntent, options.toBundle())

                            }
                        }

                    }

        }

        sendButton.setOnClickListener( onClickListener )

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
                .setMessage("Wrong sms code")
                .setPositiveButton(android.R.string.ok, { _, _ -> })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show()

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
