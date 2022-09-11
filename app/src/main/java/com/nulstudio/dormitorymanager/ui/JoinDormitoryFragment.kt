package com.nulstudio.dormitorymanager.ui

import android.content.DialogInterface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.account.DormitoryManager
import com.nulstudio.dormitorymanager.exception.DormitoryManagerException
import com.nulstudio.dormitorymanager.sys.NulApplication
import java.lang.Exception


class JoinDormitoryFragment : Fragment() {

    private lateinit var buttonBack: Button
    private lateinit var buttonNext: Button

    private lateinit var editCode: EditText

    private val handler = Handler.createAsync(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_join_dormitory, container, false)

        val activity = requireActivity() as SelectDormitoryActivity

        buttonBack = view.findViewById(R.id.btn_join_dorm_back)
        buttonNext = view.findViewById(R.id.btn_join_dorm_next)
        editCode = view.findViewById(R.id.edit_join_dorm_code)

        buttonBack.setOnClickListener {
            activity.onBackPressed()
        }

        editCode.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                buttonNext.isEnabled = editCode.text.isNotEmpty()
            }
        })

        buttonNext.setOnClickListener {
            NulApplication.executorService.submit {
                try {
                    DormitoryManager.joinDormitory(activity, editCode.text.toString())
                    val fragment = JoinDoneFragment.newInstance()
                    activity.status++
                    parentFragmentManager.beginTransaction().setCustomAnimations(
                        R.anim.fragment_reg_trans_in, R.anim.fragment_login_trans_out,
                        R.anim.fragment_login_trans_in, R.anim.fragment_reg_trans_out
                    )
                        .replace(R.id.frame_select_dorm_fragment, fragment)
                        .addToBackStack(null).commit()
                } catch (e: DormitoryManagerException) {
                    handler.post {
                        Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    handler.post {
                        AlertDialog.Builder(activity).setTitle("Error - Debug Assert")
                            .setMessage(e.toString())
                            .setPositiveButton("OK") { _, _ -> }
                            .show()
                    }
                }
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            JoinDormitoryFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}