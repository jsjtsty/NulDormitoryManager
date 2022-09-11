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


class CreateDormitoryFragment : Fragment() {

    private lateinit var buttonBack: Button
    private lateinit var buttonNext: Button

    private lateinit var editName: EditText
    private lateinit var editDescription: EditText

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
        val view = inflater.inflate(R.layout.fragment_create_dormitory, container, false)

        val activity = requireActivity() as SelectDormitoryActivity

        buttonBack = view.findViewById(R.id.btn_create_dorm_back)
        buttonNext = view.findViewById(R.id.btn_create_dorm_next)

        editName = view.findViewById(R.id.edit_create_dorm_name)
        editDescription = view.findViewById(R.id.edit_create_dorm_desp)

        buttonBack.setOnClickListener {
            activity.onBackPressed()
        }

        editName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(p0: Editable?) {
                buttonNext.isEnabled = editName.text.isNotEmpty()
            }
        })

        buttonNext.setOnClickListener {
            NulApplication.executorService.submit {
                try {
                    DormitoryManager.createDormitory(activity, editName.text.toString(), editDescription.text.toString())
                    val fragment = JoinDoneFragment.newInstance(false, DormitoryManager.code)
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
                            .setPositiveButton("OK") { _: DialogInterface, _: Int ->

                            }.show()
                    }
                }
            }
        }

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            CreateDormitoryFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}