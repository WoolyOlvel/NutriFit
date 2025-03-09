package com.ascrib.nutrifit.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.fragment.app.FragmentActivity
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.PageBienvenidaBinding
import com.ascrib.nutrifit.ui.form.adapter.BienvenidaAdapter

class BienvenidaPage: Fragment() {

    lateinit var binding: PageBienvenidaBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.page_bienvenida, container, false)
        binding.handler = this

        (activity as FormActivity).statusBarIconDark(true)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val pagerAdapter = BienvenidaAdapter(context as FragmentActivity)

        binding.vpBienvenida.adapter = pagerAdapter
        binding.dotsIndicator.setViewPager2(binding.vpBienvenida)

    }

}