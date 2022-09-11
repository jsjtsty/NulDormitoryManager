package com.nulstudio.dormitorymanager.ui

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import com.nulstudio.dormitorymanager.R
import pl.droidsonroids.gif.GifDrawable
import pl.droidsonroids.gif.GifDrawableBuilder
import pl.droidsonroids.gif.GifImageView


class MainPageActivity : AppCompatActivity() {

    private var currentPosition: Int = 0

    private var isBackPressed: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_page)

        val normalColor = getColor(R.color.black)
        val selectedColor = getColor(R.color.light_blue)

        val layoutList: List<ConstraintLayout> =
            listOf(
                findViewById(R.id.layout_main_page_main),
                findViewById(R.id.layout_main_page_discuss),
                findViewById(R.id.layout_main_page_plan),
                findViewById(R.id.layout_main_page_my)
            )

        val layoutNumber: Map<Int, Int> = mapOf(
            R.id.layout_main_page_main to 0,
            R.id.layout_main_page_discuss to 1,
            R.id.layout_main_page_plan to 2,
            R.id.layout_main_page_my to 3
        )

        val imgList: List<GifImageView> =
            listOf(
                findViewById(R.id.img_main_page_main),
                findViewById(R.id.img_main_page_discuss),
                findViewById(R.id.img_main_page_plan),
                findViewById(R.id.img_main_page_my)
            )

        val normalDrawableList: List<Drawable> =
            listOf(
                AppCompatResources.getDrawable(this, R.drawable.icon_home_btn_home)!!,
                AppCompatResources.getDrawable(this, R.drawable.icon_home_btn_message)!!,
                AppCompatResources.getDrawable(this, R.drawable.icon_home_btn_dynamic)!!,
                AppCompatResources.getDrawable(this, R.drawable.icon_home_btn_me)!!
            )

        val selectedDrawableList: List<GifDrawable> =
            listOf(
                GifDrawable(resources, R.drawable.icon_home_btn_home_selected_animated),
                GifDrawable(resources, R.drawable.icon_home_btn_message_selected_animated),
                GifDrawable(resources, R.drawable.icon_home_btn_dynamic_selected_animated),
                GifDrawable(resources, R.drawable.icon_home_btn_me_selected_animated)
            )

        val tvList: List<TextView> =
            listOf(
                findViewById(R.id.tv_main_page_main),
                findViewById(R.id.tv_main_page_discuss),
                findViewById(R.id.tv_main_page_plan),
                findViewById(R.id.tv_main_page_my)
            )

        val tabListener = View.OnClickListener {
            val position: Int = layoutNumber[it.id]!!

            for(i in 0..3) {
                if(i == position) {
                    imgList[i].setImageDrawable(selectedDrawableList[i])
                    selectedDrawableList[i].loopCount = 1
                    selectedDrawableList[i].start()
                    tvList[i].setTextColor(selectedColor)
                } else {
                    imgList[i].setImageDrawable(normalDrawableList[i])
                    selectedDrawableList[i].reset()
                    tvList[i].setTextColor(normalColor)
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(
                    R.id.frame_main_page,
                    when(position) {
                        0 -> MainPageFragment.newInstance()
                        1 -> ForumFragment.newInstance()
                        2 -> PlanFragment.newInstance()
                        else -> MyFragment.newInstance()
                    }
                )
                .addToBackStack(null)
                .commit()

            currentPosition = position
        }

        for(tab in layoutList) {
            tab.setOnClickListener(tabListener)
        }

        imgList[0].setImageDrawable(selectedDrawableList[0])
        selectedDrawableList[0].loopCount= 1
        selectedDrawableList[0].start()
        tvList[0].setTextColor(selectedColor)

        for(i in 1..3) {
            imgList[i].setImageDrawable(normalDrawableList[i])
            selectedDrawableList[i].reset()
            tvList[i].setTextColor(normalColor)
        }

        val mainPageFragment = MainPageFragment.newInstance()
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.frame_main_page, mainPageFragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    override fun onBackPressed() {
        if(isBackPressed) {
            finish()
        } else {
            Toast.makeText(this, "再按一次退出", Toast.LENGTH_SHORT).show()
            val timer  = object: CountDownTimer(1000, 1000) {
                override fun onTick(p0: Long) { }

                override fun onFinish() {
                    isBackPressed = false
                }
            }
            isBackPressed = true
            timer.start()
        }
    }
}