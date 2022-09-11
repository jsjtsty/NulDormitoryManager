package com.nulstudio.dormitorymanager.ui

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AlphabetIndexer
import android.widget.ImageView
import android.widget.Toast
import com.nulstudio.dormitorymanager.R

class MainPageFragment : Fragment() {

    private lateinit var imageTitle: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_main_page, container, false)
        imageTitle = view.findViewById(R.id.img_main_page_title)

        return view
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            MainPageFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}