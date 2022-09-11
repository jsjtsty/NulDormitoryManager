package com.nulstudio.dormitorymanager.ui

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.nulstudio.dormitorymanager.R

class LoginActivity : AppCompatActivity() {
    private lateinit var textCopyright: TextView
    private lateinit var textTitle: TextView
    private lateinit var buttonBack: ImageView
    private lateinit var contentLayout: FrameLayout

    private var isRegisterMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        textCopyright = findViewById(R.id.tv_login_copyright)
        textTitle = findViewById(R.id.tv_post_detail_title)
        contentLayout = findViewById(R.id.frame_login_fragment)
        buttonBack = findViewById(R.id.btn_post_detail_back)

        val loginFragment = LoginFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.frame_login_fragment, loginFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    fun requestModeSwitch() {
        if(isRegisterMode) {
            textTitle.text = getString(R.string.txt_login)
        } else {
            textTitle.text = getString(R.string.txt_register)
        }

        isRegisterMode = !isRegisterMode
    }

    fun setBackButtonOnClickListener(listener: View.OnClickListener) {
        buttonBack.setOnClickListener(listener)
    }

    override fun onBackPressed() {
        if(!isRegisterMode) {
            finish()
        } else {
            supportFragmentManager.popBackStack()
        }
    }
}