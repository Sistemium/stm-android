package com.sistemium.sissales.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AppCompatActivity
import android.view.KeyEvent
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import com.sistemium.sissales.R
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController
import kotlinx.android.synthetic.main.activity_code_confirm.*
import nl.komponents.kovenant.then


class CodeConfirmActivity : AppCompatActivity() {

    private var mobileNumber: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_code_confirm)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val id = intent.getStringExtra("ID")
        mobileNumber = intent.getStringExtra("mobileNumber")

        val sendButton: Button = findViewById(R.id.button)
        val smsCodeEdit: EditText = findViewById(R.id.editText)

        smsCodeEdit.setOnKeyListener(View.OnKeyListener { _, keyCode, event ->

            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action != KeyEvent.ACTION_UP) {

                sendButton.performClick()

                return@OnKeyListener true

            }

            false
        })


        val onClickListener = View.OnClickListener {

            val spinner: ConstraintLayout = findViewById(R.id.loading_screen)

            spinner.visibility = View.VISIBLE

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

            supportActionBar?.setDisplayHomeAsUpEnabled(false)

            STMCoreAuthController.requestAccessToken(id, smsCodeEdit.text.toString()) then {

                STMCoreAuthController.logIn() then {

                    this.runOnUiThread {

                        spinner.visibility = View.INVISIBLE

                        supportActionBar?.setDisplayHomeAsUpEnabled(true)

                    }

                    finish()

                }

            } fail {

                this.runOnUiThread {

                    spinner.visibility = View.INVISIBLE

                    supportActionBar?.setDisplayHomeAsUpEnabled(true)

                }

                STMFunctions.handleError(this, it.localizedMessage)

            }

        }

        sendButton.setOnClickListener(onClickListener)

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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

    override fun onBackPressed() {

        val spinner: ConstraintLayout = findViewById(R.id.loading_screen)

        if (spinner.visibility == View.INVISIBLE) {

            super.onBackPressed()

        }

    }

}