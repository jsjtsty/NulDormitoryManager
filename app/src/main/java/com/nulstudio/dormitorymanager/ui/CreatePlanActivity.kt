package com.nulstudio.dormitorymanager.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.component.CircleSeekBar

class CreatePlanActivity : AppCompatActivity() {

    private lateinit var textHour: TextView
    private lateinit var textMinute: TextView
    private lateinit var progressBar: CircleSeekBar
    private lateinit var buttonBack: ImageView
    private lateinit var buttonDone: Button
    private lateinit var editName: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_plan)

        textHour = findViewById(R.id.tv_create_plan_hour)
        textMinute = findViewById(R.id.tv_create_plan_min)
        progressBar = findViewById(R.id.seek_create_plan_time)
        buttonBack = findViewById(R.id.btn_create_plan_back)
        buttonDone = findViewById(R.id.btn_create_plan_ok)
        editName = findViewById(R.id.edit_create_plan_name)

        progressBar.setOnProgressChangeListener(object: CircleSeekBar.OnProgressChangeListener {
            override fun onProgressChange(view: View, value: Float) {
                textHour.text = (value.toInt() / 60).toString()
                textMinute.text = (value.toInt() % 60).toString()
            }
        })

        buttonBack.setOnClickListener {
            setResult(-1)
            finish()
        }

        buttonDone.setOnClickListener {
            val intent: Intent = Intent()
            intent.putExtra("time", progressBar.value.toInt())
            intent.putExtra("name", editName.text.toString())
            setResult(0, intent)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}