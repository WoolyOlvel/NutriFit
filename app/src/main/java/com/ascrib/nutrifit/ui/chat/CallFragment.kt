package com.ascrib.nutrifit.ui.chat

import android.os.Bundle
import android.view.*
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentCallBinding
import java.util.*

class CallFragment : Fragment() {
    lateinit var binding: FragmentCallBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_call, container, false)
        binding.handler = this

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }


    fun callEnd(){
        findNavController().navigateUp()
    }
}