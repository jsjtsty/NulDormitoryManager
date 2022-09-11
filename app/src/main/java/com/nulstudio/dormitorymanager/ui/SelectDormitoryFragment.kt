package com.nulstudio.dormitorymanager.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.cardview.widget.CardView
import com.nulstudio.dormitorymanager.R
import com.nulstudio.dormitorymanager.exception.NulRuntimeException


class SelectDormitoryFragment : Fragment() {

    private lateinit var cardCreateDormitory: CardView
    private lateinit var cardJoinDormitory: CardView

    private lateinit var textCreateDormitoryTitle: TextView
    private lateinit var textJoinDormitoryTitle: TextView
    private lateinit var textCreateDormitoryDescription: TextView
    private lateinit var textJoinDormitoryDescription: TextView

    private lateinit var buttonNext: Button

    private var selection: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_select_dormitory, container, false)

        val activity = requireActivity() as SelectDormitoryActivity

        cardCreateDormitory = view.findViewById(R.id.card_select_dorm_create)
        cardJoinDormitory = view.findViewById(R.id.card_select_dorm_join)

        textCreateDormitoryTitle = view.findViewById(R.id.tv_select_dorm_create_title)
        textJoinDormitoryTitle = view.findViewById(R.id.tv_select_dorm_join_title)
        textCreateDormitoryDescription = view.findViewById(R.id.tv_select_dorm_create_desp)
        textJoinDormitoryDescription = view.findViewById(R.id.tv_select_dorm_join_desp)

        buttonNext = view.findViewById(R.id.btn_select_dorm_next)

        cardCreateDormitory.setOnClickListener {
            selection = 1
            buttonNext.isEnabled = true

            cardCreateDormitory.setCardBackgroundColor(activity.getColor(R.color.light_blue))
            cardJoinDormitory.setCardBackgroundColor(activity.getColor(R.color.white))

            textCreateDormitoryTitle.setTextColor(activity.getColor(R.color.white))
            textCreateDormitoryDescription.setTextColor(activity.getColor(R.color.white))
            textJoinDormitoryTitle.setTextColor(activity.getColor(R.color.black))
            textJoinDormitoryDescription.setTextColor(activity.getColor(R.color.black))
        }

        cardJoinDormitory.setOnClickListener {
            selection = 2
            buttonNext.isEnabled = true

            cardCreateDormitory.setCardBackgroundColor(activity.getColor(R.color.white))
            cardJoinDormitory.setCardBackgroundColor(activity.getColor(R.color.light_blue))

            textCreateDormitoryTitle.setTextColor(activity.getColor(R.color.black))
            textCreateDormitoryDescription.setTextColor(activity.getColor(R.color.black))
            textJoinDormitoryTitle.setTextColor(activity.getColor(R.color.white))
            textJoinDormitoryDescription.setTextColor(activity.getColor(R.color.white))
        }

        buttonNext.setOnClickListener {
            ++activity.status
            val fragment = when(selection) {
                1 -> CreateDormitoryFragment.newInstance()
                else -> JoinDormitoryFragment.newInstance()
            }
            parentFragmentManager.beginTransaction().setCustomAnimations(
                R.anim.fragment_reg_trans_in, R.anim.fragment_login_trans_out,
                R.anim.fragment_login_trans_in, R.anim.fragment_reg_trans_out
            )
                .replace(R.id.frame_select_dorm_fragment, fragment)
                .addToBackStack(null).commit()
        }

        return view
    }

    companion object {

        @JvmStatic
        fun newInstance() =
            SelectDormitoryFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}