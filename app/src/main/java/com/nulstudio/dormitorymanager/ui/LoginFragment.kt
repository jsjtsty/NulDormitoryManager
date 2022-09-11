package com.nulstudio.dormitorymanager.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.account.AccountManager
import com.nulstudio.dormitorymanager.account.DormitoryManager
import com.nulstudio.dormitorymanager.exception.LoginException
import com.nulstudio.dormitorymanager.exception.NulRuntimeException
import com.nulstudio.dormitorymanager.sys.NulApplication

class LoginFragment : Fragment() {

    private lateinit var editUserName: EditText
    private lateinit var editPassword: EditText
    private lateinit var buttonLogin: Button
    private lateinit var buttonRegister: Button

    private lateinit var activity: LoginActivity

    private var inviteCode: String = ""

    private val listenerBack = View.OnClickListener {
        activity.finish()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        val handler = Handler.createAsync(Looper.getMainLooper())

        activity = requireActivity() as LoginActivity

        editUserName = view.findViewById(R.id.edit_login_user_name)
        editPassword = view.findViewById(R.id.edit_login_password)
        buttonLogin = view.findViewById(R.id.btn_login)
        buttonRegister = view.findViewById(R.id.btn_register)

        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun afterTextChanged(editable: Editable) {
                if (editable.isEmpty()) {
                    buttonLogin.isEnabled = false
                } else {
                    if (editUserName.text.isNotEmpty() && editPassword.text.isNotEmpty()) {
                        buttonLogin.isEnabled = true
                    }
                }
            }
        }

        editPassword.addTextChangedListener(textWatcher)
        editUserName.addTextChangedListener(textWatcher)

        activity.setBackButtonOnClickListener(listenerBack)

        parentFragmentManager.setFragmentResultListener(
            "register", this
        ) { _, result ->
            activity.requestModeSwitch()
            activity.setBackButtonOnClickListener(listenerBack)
            editUserName.setText(result.getString(RegisterFragment.PARAM_USER_NAME))
            editPassword.setText(result.getString(RegisterFragment.PARAM_PASSWORD))
            inviteCode = result.getString(RegisterFragment.PARAM_INVITE_CODE) ?: ""

            if(editPassword.text.isNotEmpty() || editUserName.text.isNotEmpty()) {
                buttonRegister.isEnabled = true
            }
        }

        buttonLogin.setOnClickListener {
            login(handler)
        }

        buttonRegister.setOnClickListener {
            activity.requestModeSwitch()
            parentFragmentManager.beginTransaction()
                .setCustomAnimations(
                    R.anim.fragment_reg_trans_in, R.anim.fragment_login_trans_out,
                    R.anim.fragment_login_trans_in, R.anim.fragment_reg_trans_out
                )
                .replace(
                    R.id.frame_login_fragment,
                    RegisterFragment.newInstance(
                        editUserName.text.toString(),
                        editPassword.text.toString(), inviteCode
                    )
                )
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun login(handler: Handler) {
        NulApplication.executorService.submit {
            try {
                AccountManager.login(activity, editUserName.text.toString(), editPassword.text.toString())
                DormitoryManager.update()
                val objActivity = if(DormitoryManager.isBound) MainPageActivity::class.java
                    else SelectDormitoryActivity::class.java
                //val objActivity = SelectDormitoryActivity::class.java
                activity.startActivity(Intent(activity, objActivity))
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
                handler.post {
                    AlertDialog.Builder(activity).setTitle("Error - Debug Assert")
                        .setMessage(e.message)
                        .setPositiveButton("OK") { _: DialogInterface, _: Int ->

                        }.show()
                }
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            LoginFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}