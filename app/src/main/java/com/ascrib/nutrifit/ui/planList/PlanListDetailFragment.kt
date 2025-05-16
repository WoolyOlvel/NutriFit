package com.ascrib.nutrifit.ui.planList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.*
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentHistoryNutriListBinding
import com.ascrib.nutrifit.databinding.FragmentHistoryNutriListInfoBinding
//import com.ascrib.nutrifit.ui.dashboard.adapter.AppointmentAdapter
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModel
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModelFactory
import com.ascrib.nutrifit.util.getStatusBarHeight

class PlanListDetailFragment : Fragment() {

    lateinit var binding: FragmentHistoryNutriListInfoBinding


    lateinit var model: DashboardViewModel

//    lateinit var appointmentAdapter: AppointmentAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_history_nutri_list_info,
            container, false)

        binding.handler = this

        model = ViewModelProvider(this, DashboardViewModelFactory())[DashboardViewModel::class.java]

        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val appointmentStatus = arguments?.get("appointment") as? Int

        if (appointmentStatus == 3) { // Realizado
            binding.textConsulFinish.visibility = View.VISIBLE
        } else if (appointmentStatus == 4) { // Completado
            binding.textCancel.visibility = View.VISIBLE


        } else {
            // Si no es 2 o 4, ocultamos todo y deshabilitamos interacción
            binding.root.visibility = View.GONE
            binding.root.isClickable = false
            binding.root.isFocusable = false
        }
    }


    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Información Consulta"
        binding.toolbar.toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.header_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }R.id.action_notification ->{
                        findNavController().navigate(R.id.global_notificationFragment)
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }





}