package com.ascrib.nutrifit.ui.planList

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentHistoryNutriListBinding
import com.ascrib.nutrifit.handler.AppointmentHandler
import com.ascrib.nutrifit.model.Appointment
//import com.ascrib.nutrifit.ui.dashboard.adapter.AppointmentAdapter
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModel
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModelFactory
import com.ascrib.nutrifit.util.getStatusBarHeight

class HistoryNutriListFragment : Fragment(), AppointmentHandler {

    lateinit var binding: FragmentHistoryNutriListBinding

    lateinit var model: DashboardViewModel

//    lateinit var appointmentAdapter: AppointmentAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding =  DataBindingUtil.inflate(inflater, R.layout.fragment_history_nutri_list, container, false)
        binding.handler = this

        model = ViewModelProvider(this, DashboardViewModelFactory())[DashboardViewModel::class.java]

        toolbarConfig()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        makeAppointment()
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Historial Nutricional"
        binding.toolbar.toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

                menuInflater.inflate(R.menu.header_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }
                    R.id.action_notification ->{
                        findNavController().navigate(R.id.global_notificationFragment)
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

//    fun makeAppointment() {
//        val inProgressAdapter = AppointmentAdapter(ArrayList(model.getInProgressAppointments()), this)
//        val nextConsultsAdapter = AppointmentAdapter(ArrayList(model.getNextAppointments()), this)
//        val pastConsultsAdapter = AppointmentAdapter(ArrayList(model.getPastAppointments()), this)
//
//        binding.recyclerviewInProgress.apply {
//            adapter = inProgressAdapter
//            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        }
//
//        binding.recyclerviewNextConsults.apply {
//            adapter = nextConsultsAdapter
//            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        }
//
//        binding.recyclerviewPastConsults.apply {
//            adapter = pastConsultsAdapter
//            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
//        }
//    }

    override fun appointmentClicked(appointment: Appointment) {
        // Verifica si el estado de la cita es 3 o 4
        if (appointment.statusType == 3 || appointment.statusType == 4) {
            // Si es 3 o 4, navega a global_planListDetailFragment
            findNavController().navigate(
                R.id.global_planListDetailFragment,
                bundleOf("appointment" to appointment.statusType)
            )
        } else {
            // Si el estado no es 3 ni 4, no hace nada
            // (puedes agregar alguna lógica adicional aquí si es necesario)
        }
    }



}