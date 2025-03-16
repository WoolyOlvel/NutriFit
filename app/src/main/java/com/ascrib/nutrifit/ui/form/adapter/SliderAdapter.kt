package com.ascrib.nutrifit.ui.form.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ascrib.nutrifit.ui.form.BienvenidaSlider1Fragment
import com.ascrib.nutrifit.ui.form.BienvenidaSlider2Fragment
import com.ascrib.nutrifit.ui.form.BienvenidaSlider3Fragment
import com.ascrib.nutrifit.ui.form.SliderBanner1Fragment
import com.ascrib.nutrifit.ui.form.SliderBanner2Fragment
import com.ascrib.nutrifit.ui.form.SliderBanner3Fragment

class SliderAdapter (fa: FragmentActivity): FragmentStateAdapter(fa) {

    override fun getItemCount(): Int {
        return 3
    }

    override fun createFragment(position: Int): Fragment {
        return when (position){
            0 -> {
                SliderBanner1Fragment()
            }
            1 -> {
                SliderBanner2Fragment()
            }
            2 -> {
                SliderBanner3Fragment()
            }
            else -> SliderBanner1Fragment()
        }
    }

}