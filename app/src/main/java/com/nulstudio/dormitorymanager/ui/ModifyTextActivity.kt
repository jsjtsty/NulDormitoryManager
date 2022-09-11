package com.nulstudio.dormitorymanager.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.nulstudio.dormitorymanager.R

class ModifyTextActivity : AppCompatActivity() {
    private lateinit var editContent: EditText
    private lateinit var buttonNext: Button
    private lateinit var buttonBack: ImageView
    private lateinit var textCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_modify_text)

        editContent = findViewById(R.id.edit_modify_content)
        buttonBack = findViewById(R.id.btn_modify_back)
        buttonNext = findViewById(R.id.btn_modify_next)
        textCount = findViewById(R.id.tv_modify_count)

        buttonBack.setOnClickListener {
            finish()
        }

        editContent.addTextChangedListener {
            textCount.text = editContent.text.length.toString() + "/20"
        }

        buttonNext.setOnClickListener {
            setResult(100, Intent().apply {
                putExtra("value", editContent.text.toString())
            })
            finish()
        }
    }
}