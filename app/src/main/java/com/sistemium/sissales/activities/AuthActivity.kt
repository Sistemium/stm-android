package com.sistemium.sissales.activities

import android.annotation.SuppressLint
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity;
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import androidx.constraintlayout.widget.ConstraintLayout
import com.sistemium.sissales.BuildConfig
import com.sistemium.sissales.R
import com.sistemium.sissales.base.MyApplication
import com.sistemium.sissales.base.STMCoreSessionFiler
import com.sistemium.sissales.base.STMFunctions
import com.sistemium.sissales.base.session.STMCoreAuthController
import com.sistemium.sissales.base.session.STMSession
import kotlinx.android.synthetic.main.activity_auth.*
import nl.komponents.kovenant.then

class AuthActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isTaskRoot) {
            finish()
            return
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        STMCoreAuthController.logIn() then {

            finish()

        } fail {

            runOnUiThread {

                setup()

            }

        }
    }

    @SuppressLint("PrivateResource")
    private fun setup() {

        if (BuildConfig.APPLICATION_ID.contains(".vfs")){

            val intent = Intent(this, WebViewActivity::class.java)

            val url = "https://vfsm.sistemium.com"

            intent.putExtra("url", url)
            intent.putExtra("title", "VFS")
            startActivity(intent)

            return

        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        setContentView(R.layout.activity_auth)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val phoneNumberEdit: EditText = findViewById(R.id.editText)
        val sendButton: Button = findViewById(R.id.button)
        val intent = intent
        val mobileNumber = intent.getStringExtra("mobileNumber")
        phoneNumberEdit.setText(mobileNumber)
        phoneNumberEdit.setSelection(phoneNumberEdit.text.length)

        val listener = View.OnKeyListener { _, keyCode, event ->

            if (keyCode == KeyEvent.KEYCODE_ENTER && event.action != KeyEvent.ACTION_UP) {

                sendButton.performClick()

                return@OnKeyListener true

            }

            false
        }

        phoneNumberEdit.setOnKeyListener(listener)

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

        val onClickListener = View.OnClickListener {

            val spinner: ConstraintLayout = findViewById(R.id.loading_screen)

            spinner.visibility = View.VISIBLE

            val view = this.currentFocus
            if (view != null) {
                val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(view.windowToken, 0)
            }

            STMCoreAuthController.requestNewSMSCode(phoneNumberEdit.text.toString()) then {

                val myIntent = Intent(this@AuthActivity, CodeConfirmActivity::class.java)
                myIntent.putExtra("ID", it)
                myIntent.putExtra("mobileNumber", phoneNumberEdit.text.toString())

                val options = ActivityOptions.makeCustomAnimation(this, R.anim.abc_fade_in, R.anim.abc_fade_out)

                this@AuthActivity.startActivity(myIntent, options.toBundle())

                this.runOnUiThread {

                    spinner.visibility = View.INVISIBLE

                }

            } fail {

                this.runOnUiThread {

                    spinner.visibility = View.INVISIBLE

                }

                STMFunctions.handleError(this, it.localizedMessage)

            }

        }

        sendButton.setOnClickListener(onClickListener)

        val demo: Button = findViewById(R.id.demo)

        val demoOnClickListener = View.OnClickListener {

            STMCoreAuthController.accountOrg = "DEMO org"
            STMCoreAuthController.iSisDB = "demo_sales"
            STMCoreAuthController.userID = "user id DEMO"
            STMCoreAuthController.isDemo = true

            STMSession.sharedSession!!.persistenceDelegate

            val myIntent = Intent(MyApplication.appContext, ProfileActivity::class.java)

            myIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK

            val options = ActivityOptions.makeCustomAnimation(MyApplication.appContext, R.anim.abc_fade_in, R.anim.abc_fade_out)

            MyApplication.appContext?.startActivity(myIntent, options.toBundle())

        }

        demo.setOnClickListener(demoOnClickListener)

    }

}
