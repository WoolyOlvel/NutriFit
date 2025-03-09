package com.ascrib.nutrifit.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentBienvenidaSlider2Binding

class BienvenidaSlider2Fragment: Fragment() {

    lateinit var binding: FragmentBienvenidaSlider2Binding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_bienvenida_slider2, container, false)

        (activity as FormActivity).statusBarIconDark(true)
        return binding.root
    }

}