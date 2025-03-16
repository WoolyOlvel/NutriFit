package com.ascrib.nutrifit.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentSliderBannerHomeBinding

class SliderBanner1Fragment : Fragment() {

    lateinit var binding: FragmentSliderBannerHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_slider_banner_home, container, false)

        // Elimina o comenta esta línea
        // (activity as FormActivity).statusBarIconDark(true)

        // O, si necesitas configurar la barra de estado, usa una comprobación
        try {
            if (activity is FormActivity) {
                (activity as FormActivity).statusBarIconDark(true)
            }
        } catch (e: Exception) {
            // Manejar silenciosamente si no es una FormActivity
        }

        return binding.root
    }
}