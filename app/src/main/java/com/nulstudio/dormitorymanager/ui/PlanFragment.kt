package com.nulstudio.dormitorymanager.ui

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import com.nulstudio.dormitorymanager.R


class PlanFragment : Fragment() {

    private lateinit var buttonMenu: ImageView
    private lateinit var buttonMore: ImageView
    private lateinit var viewProgress1: View
    private lateinit var viewProgress2: View

    private lateinit var textHour: TextView
    private lateinit var textMinute: TextView

    private lateinit var layoutProgressBar: LinearLayout

    private lateinit var textRemaining: TextView
    private lateinit var textPlanName: TextView

    private lateinit var buttonStart: ImageView

    private lateinit var activity: MainPageActivity

    private var maxTime: Int = 30
    private var time: Long = 30 * 60

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    private fun setProgress(progress: Double) {
        viewProgress2.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT, (10000 * progress).toInt().toFloat())
        viewProgress1.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.MATCH_PARENT, (10000 * (1 - progress)).toInt().toFloat())
        if(progress < 0.2) {
            viewProgress1.setBackgroundColor(activity.getColor(R.color.circle_seekbar_negative))
            viewProgress2.setBackgroundColor(activity.getColor(R.color.circle_seekbar_negative_background))
        } else {
            viewProgress1.setBackgroundColor(Color.rgb(0x3F, 0xD2, 0x68))
            viewProgress2.setBackgroundColor(Color.rgb(0xB2, 0xED, 0xC3))
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_plan, container, false)

        activity = requireActivity() as MainPageActivity

        buttonMenu = view.findViewById(R.id.btn_plan_menu)
        buttonMore = view.findViewById(R.id.btn_plan_more)
        layoutProgressBar = view.findViewById(R.id.layout_card_bg)

        viewProgress1 = view.findViewById(R.id.view_card_part1)
        viewProgress2 = view.findViewById(R.id.view_card_part2)

        textPlanName = view.findViewById(R.id.tv_plan_name)
        textHour = view.findViewById(R.id.tv_card_hour)
        textMinute = view.findViewById(R.id.tv_card_min)
        textRemaining = view.findViewById(R.id.tv_card_remaining)

        buttonStart = view.findViewById(R.id.btn_plan_start)

        buttonMore.setOnClickListener {
            startActivityForResult(Intent(requireActivity(), CreatePlanActivity::class.java), 0)
        }


        /*
        buttonStart.setOnClickListener {
            object: CountDownTimer(time * 1000, 1000) {
                override fun onTick(p0: Long) {
                    --time
                    setProgress(time.toDouble() / (maxTime * 60))
                    textHour.text = (time / 3600).toString()
                    textMinute.text = (time / 60).toString()
                    textRemaining.text = String.format(
                        getString(R.string.txt_time_record_text_desp), ((maxTime * 60 - time) / 3600).toInt(),
                        ((maxTime * 60 - time) / 60).toInt()
                    )
                }

                override fun onFinish() {

                }

            }.start()
        }
        */

        return view
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(resultCode == 0) {
            maxTime = data?.extras?.getInt("time")!!
            time = maxTime * 60L
            textHour.text = (maxTime / 60).toString()
            textMinute.text = (maxTime % 60).toString()
            textPlanName.text = data.extras?.getString("name")!!


        }
    }

    companion object {
        fun newInstance() =
            PlanFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}