package com.nulstudio.dormitorymanager.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.account.AccountManager
import com.nulstudio.dormitorymanager.account.AvatarManager
import com.nulstudio.dormitorymanager.account.CreditManager
import com.nulstudio.dormitorymanager.account.DormitoryManager
import com.nulstudio.dormitorymanager.sys.NulApplication
import de.hdodenhof.circleimageview.CircleImageView
import java.text.SimpleDateFormat
import java.util.*

class MyFragment : Fragment() {

    private lateinit var textUserName: TextView
    private lateinit var textUID: TextView
    private lateinit var textCode: TextView
    private lateinit var textDormitorySize: TextView
    private lateinit var textMidRemainingTime: TextView
    private lateinit var textFinalRemainingTime: TextView
    private lateinit var textCoinCount: TextView
    private lateinit var textDormitoryName: TextView
    private lateinit var imageAvatar: CircleImageView

    private lateinit var activity: MainPageActivity

    private lateinit var layoutCopyCode: ConstraintLayout
    private lateinit var layoutUserInformation: ConstraintLayout

    private val handler = Handler.createAsync(Looper.getMainLooper())

    private val dateMidExam = Calendar.getInstance().apply {
        set(2022, 3, 16, 8, 0, 0)
    }

    private val dateFinalExam = Calendar.getInstance().apply {
        set(2022, 5, 13, 8, 0, 0)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_my, container, false)

        activity = requireActivity() as MainPageActivity

        val clipboardManager = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

        textUserName = view.findViewById(R.id.tv_my_user_name)
        textUID = view.findViewById(R.id.tv_my_uid)
        textCode = view.findViewById(R.id.tv_my_code)
        textDormitorySize = view.findViewById(R.id.tv_my_manage_count)
        textMidRemainingTime = view.findViewById(R.id.tv_my_schedule_mid_time)
        textFinalRemainingTime = view.findViewById(R.id.tv_my_schedule_final_time)
        textCoinCount = view.findViewById(R.id.tv_my_coins_count)
        layoutCopyCode = view.findViewById(R.id.layout_my_copy_code)
        textDormitoryName = view.findViewById(R.id.tv_my_dorm_name)
        layoutUserInformation = view.findViewById(R.id.layout_my_user)
        imageAvatar = view.findViewById(R.id.img_my_avatar)

        textUserName.text = AccountManager.nickName
        textUID.text = String.format(getString(R.string.txt_uid), AccountManager.uid)
        textCode.text = DormitoryManager.code
        textDormitorySize.text = String.format(getString(R.string.txt_manage_dorm_count), DormitoryManager.members.size)
        textDormitoryName.text = DormitoryManager.name

        val now = Calendar.getInstance().timeInMillis
        val nowToMid = dateMidExam.timeInMillis - now

        textMidRemainingTime.text = String.format(getString(R.string.txt_remaining_time), nowToMid / 1000 / 3600 / 24,
                nowToMid / 1000 / 3600 % 24, nowToMid / 1000 / 60 % 60)

        val nowToFinal = dateFinalExam.timeInMillis - now
        textFinalRemainingTime.text = String.format(getString(R.string.txt_remaining_time), nowToFinal / 1000 / 3600 / 24,
            nowToFinal / 1000 / 3600 % 24, nowToFinal / 1000 / 60 % 60)


        layoutCopyCode.setOnClickListener {
            clipboardManager.setPrimaryClip(ClipData.newPlainText("code", DormitoryManager.code))
            Toast.makeText(activity, getString(R.string.txt_copy_done), Toast.LENGTH_SHORT).show()
        }

        layoutUserInformation.setOnClickListener {
            startActivity(Intent(activity, PersonalInformationActivity::class.java).apply {
                putExtra("uid", AccountManager.uid)
            })
        }

        NulApplication.executorService.submit {
            CreditManager.update()
            AvatarManager.requestFetchAvatars(setOf(AccountManager.uid))
            handler.post {
                textCoinCount.text = CreditManager.coins.toString()
                if(AvatarManager.avatarMap[AccountManager.uid] != null) {
                    imageAvatar.setImageBitmap(AvatarManager.avatarMap[AccountManager.uid])
                }
            }
        }


        return view
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            MyFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}