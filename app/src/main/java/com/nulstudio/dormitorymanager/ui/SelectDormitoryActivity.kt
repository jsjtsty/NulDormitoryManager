package com.nulstudio.dormitorymanager.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageView
import com.nulstudio.dormitorymanager.R

class SelectDormitoryActivity : AppCompatActivity() {

    private lateinit var buttonBack: ImageView

    var status: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_dormitory)

        buttonBack = findViewById(R.id.btn_post_detail_back)
        buttonBack.setOnClickListener {
            onBackPressed()
        }

        val selectDormitoryFragment = SelectDormitoryFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.frame_select_dorm_fragment, selectDormitoryFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        if(status == 2) {
            supportFragmentManager.popBackStack()
            --status
        } else if(status == 1) {
            finish()
        }
    }
}