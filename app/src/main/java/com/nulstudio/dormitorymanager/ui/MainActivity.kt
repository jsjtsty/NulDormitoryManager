package com.nulstudio.dormitorymanager.ui

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.account.AccountManager
import com.nulstudio.dormitorymanager.account.DormitoryManager
import com.nulstudio.dormitorymanager.exception.LoginException
import com.nulstudio.dormitorymanager.sys.NulApplication
import com.nulstudio.dormitorymanager.sys.UpdateManager

class MainActivity : AppCompatActivity() {
    private lateinit var textVersion: TextView

    private var isInitialized = false
    private var isLoggedIn = false
    private val handler = Handler.createAsync(Looper.getMainLooper())

    private val timer: CountDownTimer = object : CountDownTimer(60000, 2000) {
        override fun onTick(p0: Long) {
            if(p0 < 59000 && isInitialized) {
                startActivity(
                    Intent(
                        this@MainActivity, if (!isLoggedIn) LoginActivity::class.java
                        else MainPageActivity::class.java
                    )
                )
                Toast.makeText(this@MainActivity, String.format(getString(R.string.txt_welcome_toast),
                    AccountManager.userName, AccountManager.uid), Toast.LENGTH_LONG).show()
                cancel()
                finish()
            }
        }

        override fun onFinish() {

        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textVersion = findViewById(R.id.tv_welcome_version)

        NulApplication.executorService.submit {
            try {
                // 1. Login Attempt.
                isLoggedIn = AccountManager.login(this@MainActivity)

                // 2. Check for update.
                UpdateManager.checkForUpdate()
                val build = this@MainActivity.packageManager
                    .getPackageInfo(packageName, 0).longVersionCode.toInt()
                if(UpdateManager.lastRelease != null) {
                    val lastRelease = UpdateManager.lastRelease!!
                    if(lastRelease.build > build) {
                        handler.post {
                            timer.cancel()

                            val view = layoutInflater.inflate(R.layout.dialog_update, null)
                            val dialog =
                                AlertDialog.Builder(this@MainActivity)
                                    .setView(view).create()
                            val updateButton = view.findViewById<Button>(R.id.btn_update)
                            val dismissButton = view.findViewById<TextView>(R.id.tv_update_cancel)
                            val textTitle = view.findViewById<TextView>(R.id.tv_update_title)
                            val textSize = view.findViewById<TextView>(R.id.tv_update_size)
                            val textAttr = view.findViewById<TextView>(R.id.tv_update_attr)

                            textTitle.text = String.format(getString(R.string.txt_update), lastRelease.versionString)
                            textSize.text = String.format(getString(R.string.txt_update_size),
                                (lastRelease.size / 1024 / 1024).toInt())
                            textAttr.text = String.format(getString(R.string.txt_update_attr), lastRelease.description)

                            val metrics = DisplayMetrics()
                            (this@MainActivity.getSystemService(WINDOW_SERVICE) as WindowManager)
                                .defaultDisplay.getMetrics(metrics)

                            dialog.setCanceledOnTouchOutside(false)
                            dialog.window?.setBackgroundDrawableResource(R.color.transparent)

                            dismissButton.setOnClickListener {
                                timer.start()
                                dialog.cancel()
                            }

                            updateButton.setOnClickListener {
                                Toast.makeText(this@MainActivity, R.string.txt_update_downloading, Toast.LENGTH_LONG).show()
                                NulApplication.executorService.submit {
                                    UpdateManager.downloadUpdate(this@MainActivity)
                                    timer.start()
                                    dialog.cancel()
                                }
                            }

                            dialog.show()
                            dialog.window?.setLayout(metrics.widthPixels * 3 / 4,
                                metrics.heightPixels * 4 / 7)

                        }
                    }
                }

                if(isLoggedIn)
                    DormitoryManager.update()
                isInitialized = true

            } catch (e: LoginException) {
                isInitialized = true
            } catch(e: Exception) {
                handler.post {
                    AlertDialog.Builder(this@MainActivity).setTitle("Error - Debug Assert")
                        .setMessage(e.message)
                        .setPositiveButton("OK") { _: DialogInterface, _: Int ->

                        }.show()
                }
            }
        }

        timer.start()

        textVersion.text = String.format(getString(R.string.str_welcome_version),
            packageManager.getPackageInfo(packageName, 0).versionName)

        setTransparentWindow()
    }

    private fun setTransparentWindow() {
        val window = window
        val decorView = window.decorView
        val option = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
        decorView.systemUiVisibility = option
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.statusBarColor = Color.TRANSPARENT
    }
}