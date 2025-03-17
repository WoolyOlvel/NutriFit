package com.ascrib.nutrifit.ui.patient

import android.os.Bundle
import android.view.*
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
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModel
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModelFactory
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentPatientBinding
import com.ascrib.nutrifit.handler.AppointmentHandler
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.ui.dashboard.adapter.AppointmentAdapter
import com.ascrib.nutrifit.util.getStatusBarHeight

class PatientFragment : Fragment(), AppointmentHandler {
    lateinit var binding: FragmentPatientBinding

    lateinit var model: DashboardViewModel

    lateinit var appointmentAdapter: AppointmentAdapter





    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_patient, container, false)
        binding.handler = this

        model = ViewModelProvider(this, DashboardViewModelFactory())[DashboardViewModel::class.java]

        toolbarConfig()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makeAppointment()
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Sobre Mí"
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
                    }

                    R.id.action_notification -> {
                        findNavController().navigate(R.id.global_notificationFragment)
                        return true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun makeAppointment() {
        // Clonamos la lista original para evitar modificar la misma instancia
        val copiedList = model.getAppointment().map { it.copy() }

        // Usamos la copia en el adaptador para evitar modificar la original
        appointmentAdapter = AppointmentAdapter(copiedList, this)


    }


    override fun appointmentClicked(appointment: Appointment) {
        findNavController().navigate(
            R.id.global_appointmentDetailFragment,
            bundleOf("appointment" to appointment.statusType)
        )
    }
}