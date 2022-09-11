package com.nulstudio.dormitorymanager.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
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
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.account.AccountManager
import com.nulstudio.dormitorymanager.account.DormitoryManager
import com.nulstudio.dormitorymanager.exception.LoginException
import com.nulstudio.dormitorymanager.exception.NulRuntimeException
import com.nulstudio.dormitorymanager.sys.NulApplication

class RegisterFragment : Fragment() {

    private lateinit var activity: LoginActivity

    private lateinit var editUserName: EditText
    private lateinit var editPassword: EditText
    private lateinit var editInviteCode: EditText

    private lateinit var buttonBackToLogin: Button
    private lateinit var buttonRegister: Button
    private val handler = Handler.createAsync(Looper.getMainLooper())

    private val backButtonListener = View.OnClickListener {
        parentFragmentManager.popBackStack()
    }

    override fun onDestroy() {
        super.onDestroy()
        parentFragmentManager.setFragmentResult("register",
            Bundle().apply {
                putString(PARAM_USER_NAME, editUserName.text.toString())
                putString(PARAM_INVITE_CODE, editInviteCode.text.toString())
                putString(PARAM_PASSWORD, editPassword.text.toString())
            })
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        activity = requireActivity() as LoginActivity

        editUserName = view.findViewById(R.id.edit_reg_user_name)
        editPassword = view.findViewById(R.id.edit_reg_password)
        editInviteCode = view.findViewById(R.id.edit_reg_invite_code)
        buttonBackToLogin = view.findViewById(R.id.btn_reg_back_login)
        buttonRegister = view.findViewById(R.id.btn_reg_register)

        editUserName.setText(arguments?.getString(PARAM_USER_NAME))
        editPassword.setText(arguments?.getString(PARAM_PASSWORD))
        editInviteCode.setText(arguments?.getString(PARAM_INVITE_CODE))

        if(editPassword.text.isNotEmpty() || editUserName.text.isNotEmpty()) {
            buttonRegister.isEnabled = true
        }

        buttonBackToLogin.setOnClickListener(backButtonListener)

        activity.setBackButtonOnClickListener(backButtonListener)

        buttonRegister.setOnClickListener {
            register(handler)
        }

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable) {
                if (editable.isEmpty()) {
                    buttonRegister.isEnabled = false
                } else {
                    if (editUserName.text.isNotEmpty() && editPassword.text.isNotEmpty()) {
                        buttonRegister.isEnabled = true
                    }
                }
            }
        }

        editUserName.addTextChangedListener(textWatcher)
        editPassword.addTextChangedListener(textWatcher)

        return view
    }

    private fun register(handler: Handler) {
        NulApplication.executorService.submit {
            try {
                AccountManager.register(activity, editUserName.text.toString(),
                    editPassword.text.toString(), editInviteCode.text.toString())
                activity.startActivity(Intent(activity, SelectDormitoryActivity::class.java))
                activity.finish()
            } catch(e: LoginException) {
                handler.post {
                    Toast.makeText(activity, e.message, Toast.LENGTH_SHORT).show()
                }
            } catch(e: NulRuntimeException) {
                handler.post {
                    AlertDialog.Builder(activity).setTitle("Error - Debug Assert")
                        .setMessage(e.message)
                        .setPositiveButton("OK") { _: DialogInterface, _: Int ->

                        }.show()
                }
            } catch(e: Exception) {
                e.printStackTrace()
                handler.post {
                    AlertDialog.Builder(activity).setTitle("Error - Debug Assert")
                        .setMessage(e.toString())
                        .setPositiveButton("OK") { _: DialogInterface, _: Int ->

                        }.show()
                }
            }
        }
    }

    companion object {
        const val PARAM_USER_NAME = "user_name"
        const val PARAM_PASSWORD = "password"
        const val PARAM_INVITE_CODE = "invite_code"

        @JvmStatic
        fun newInstance(userName: String, password: String, inviteCode: String) =
            RegisterFragment().apply {
                arguments = Bundle().apply {
                    putString(PARAM_USER_NAME, userName)
                    putString(PARAM_PASSWORD, password)
                    putString(PARAM_INVITE_CODE, inviteCode)
                }
            }
    }
}