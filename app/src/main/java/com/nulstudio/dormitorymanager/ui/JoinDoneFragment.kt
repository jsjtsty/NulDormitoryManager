package com.nulstudio.dormitorymanager.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.nulstudio.dormitorymanager.R
import kotlin.properties.Delegates

class JoinDoneFragment : Fragment() {

    private var isJoin: Boolean = true
    private var code: String = ""

    private lateinit var buttonCopy: Button
    private lateinit var buttonNext: Button
    private lateinit var textDone: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            code = it.getString(PARAM_CODE) ?: ""
            isJoin = it.getBoolean(PARAM_JOIN)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_join_done, container, false)

        val activity = requireActivity() as SelectDormitoryActivity
        val clipboardManager = activity.getSystemService(CLIPBOARD_SERVICE) as ClipboardManager

        buttonCopy = view.findViewById(R.id.btn_join_done_copy)
        buttonNext = view.findViewById(R.id.btn_join_done)
        textDone = view.findViewById(R.id.tv_join_done_text)

        if(isJoin) {
            buttonCopy.isVisible = false
            textDone.text = activity.getString(R.string.txt_create_done_join)
        } else {
            textDone.text = activity.getString(R.string.txt_create_done_text)
            buttonCopy.text = String.format(activity.getString(R.string.txt_copy_code), code)
            buttonCopy.setOnClickListener {
                clipboardManager.setPrimaryClip(ClipData.newPlainText("code", code))
                Toast.makeText(activity, activity.getString(R.string.txt_copy_done), Toast.LENGTH_SHORT).show()
            }
        }

        buttonNext.setOnClickListener {
            activity.startActivity(Intent(activity, MainPageActivity::class.java))
            activity.finish()
        }

        return view
    }

    companion object {

        private const val PARAM_CODE = "code"
        private const val PARAM_JOIN = "join"

        @JvmStatic
        fun newInstance(join: Boolean = true, code: String = "") =
            JoinDoneFragment().apply {
                arguments = Bundle().apply {
                    putBoolean(PARAM_JOIN, join)
                    putString(PARAM_CODE, code)
                }
            }
    }
}