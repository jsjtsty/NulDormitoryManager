package com.nulstudio.dormitorymanager.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.account.Account
import com.nulstudio.dormitorymanager.account.AccountManager
import com.nulstudio.dormitorymanager.account.AvatarManager
import com.nulstudio.dormitorymanager.account.ExpManager
import com.nulstudio.dormitorymanager.exception.LoginException
import com.nulstudio.dormitorymanager.sys.NulApplication
import org.w3c.dom.Text
import java.io.File


class PersonalInformationActivity : AppCompatActivity() {
    private lateinit var imageAvatar: ImageView
    private lateinit var cropFile: File
    private lateinit var cropFileUri: Uri

    private lateinit var textUserName: TextView
    private lateinit var textNickname: TextView
    private lateinit var textUID: TextView
    private lateinit var textLevel: TextView
    private lateinit var textLevelPrev: TextView
    private lateinit var textLevelNext: TextView
    private lateinit var textPriority: TextView
    private lateinit var textModifyAvatar: TextView
    private lateinit var textLevelDescription: TextView
    private lateinit var textInformationDescription: TextView
    private lateinit var textExp: TextView

    private lateinit var imageNicknameGo: ImageView

    private lateinit var progressExp: ProgressBar

    private lateinit var buttonBack: ImageView

    private lateinit var layoutNickname: ConstraintLayout

    private var uid: Int = 0
    private var isCurrentUser: Boolean = false

    private val handler = Handler.createAsync(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_information)

        uid = intent.getIntExtra("uid", 0)
        isCurrentUser = uid == AccountManager.uid

        imageAvatar = findViewById(R.id.img_person_avatar)
        textUserName = findViewById(R.id.tv_person_user_name)
        textNickname = findViewById(R.id.tv_person_nickname)
        textUID = findViewById(R.id.tv_person_uid)
        textPriority = findViewById(R.id.tv_person_priority)
        textLevel = findViewById(R.id.tv_person_level)
        textLevelPrev = findViewById(R.id.tv_person_level_prev)
        textLevelNext = findViewById(R.id.tv_person_level_next)
        textExp = findViewById(R.id.tv_person_exp_demand)
        textModifyAvatar = findViewById(R.id.tv_person_change_avatar)
        textLevelDescription = findViewById(R.id.tv_person_level_desp)
        textInformationDescription = findViewById(R.id.tv_person_info_desp)
        buttonBack = findViewById(R.id.btn_person_back)
        imageNicknameGo = findViewById(R.id.img_person_nickname_go)
        progressExp = findViewById(R.id.progress_person_level)
        layoutNickname = findViewById(R.id.layout_person_nickname)


        buttonBack.setOnClickListener {
            this.onBackPressed()
        }

        if(!isCurrentUser) {
            imageAvatar.isEnabled = false
            imageNicknameGo.visibility = View.INVISIBLE

            textInformationDescription.text = getString(R.string.txt_his_info)
            textLevelDescription.text = getString(R.string.txt_his_level)
            textModifyAvatar.visibility = View.INVISIBLE
        } else {
            cropFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).absolutePath + "/nul_dorm_mgr_crop_image.jpg")
            if(!cropFile.exists())
                cropFile.createNewFile()
            cropFileUri = Uri.fromFile(cropFile)


            if(AvatarManager.avatarMap[AccountManager.uid] != null)
                imageAvatar.setImageBitmap(AvatarManager.avatarMap[AccountManager.uid])

            imageAvatar.setOnClickListener {
                if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                        this, arrayOf(
                            Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        ), ACTION_PERMISSION_READ_STORAGE
                    )
                } else {
                    startActivityForResult(
                        Intent(
                            Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                        ).apply { type = "image/*" }, ACTION_SELECT_PICTURE
                    )
                }
            }
        }

        if(isCurrentUser) {
            layoutNickname.setOnClickListener {
                startActivityForResult(Intent(this, ModifyTextActivity::class.java), 1)
            }
        }

        NulApplication.executorService.submit {
            val exp = ExpManager(uid)
            exp.update()
            val account = Account(uid)
            account.update()

            handler.post {
                textLevel.text = String.format(getString(R.string.txt_level_val), exp.level)
                textLevelPrev.text = String.format(getString(R.string.txt_level_val), exp.level)
                textLevelNext.text = String.format(getString(R.string.txt_level_val), exp.level + 1)
                textExp.text = String.format(
                    getString(R.string.txt_exp_demand),
                    exp.currentLevelExp,
                    exp.currentLevelDemand
                )
                progressExp.max = exp.currentLevelDemand
                progressExp.progress = exp.currentLevelExp
                textUserName.text = account.userName
                textNickname.text = account.nickName
                textUID.text = uid.toString()
                textPriority.text = getString(account.priorityStringResId)

                if (account.avatarBitmap != null) {
                    imageAvatar.setImageBitmap(account.avatarBitmap)
                }
            }
        }
    }

    companion object {
        private const val ACTION_PERMISSION_READ_STORAGE = 1
        private const val ACTION_SELECT_PICTURE = 2
        private const val ACTION_CROP_PICTURE = 3
    }

    private fun setAvatar(bitmap: Bitmap) {
        NulApplication.executorService.submit {
            AvatarManager.setAvatar(bitmap)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ACTION_SELECT_PICTURE && resultCode == RESULT_OK) {
            val selectedImage: Uri = data?.data ?: return
            val intent = Intent("com.android.camera.action.CROP").apply {
                setDataAndType(selectedImage, "image/*")
                putExtra("crop", "true");
                putExtra("aspectX", 1)
                putExtra("aspectY", 1)
                putExtra("outputX", 500)
                putExtra("outputY", 500)
                putExtra("scale", true)
                putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString())
                putExtra(MediaStore.EXTRA_OUTPUT, cropFileUri);
                putExtra("return-data", false)
            }
            startActivityForResult(intent, ACTION_CROP_PICTURE)
        } else if(requestCode == ACTION_CROP_PICTURE) {
            val bitmap = BitmapFactory.decodeStream(
                contentResolver.openInputStream(cropFileUri)
            )
            setAvatar(bitmap)
            imageAvatar.setImageBitmap(bitmap)
        } else if(resultCode == 100) {
            NulApplication.executorService.submit {
                try {
                    AccountManager.modifyNickname(
                        this@PersonalInformationActivity,
                        data?.getStringExtra("value")!!
                    )
                    handler.post {
                        Toast.makeText(this@PersonalInformationActivity, "修改完成", Toast.LENGTH_LONG)
                            .show()
                        textUserName.text = AccountManager.userName
                    }
                }
                catch (e: LoginException) {
                    handler.post {
                        Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}