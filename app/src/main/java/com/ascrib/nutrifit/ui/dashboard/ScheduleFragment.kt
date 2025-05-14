package com.ascrib.nutrifit.ui.dashboard

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
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
import com.ascrib.nutrifit.databinding.FragmentScheduleBinding
import com.ascrib.nutrifit.handler.AppointmentHandler
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.ui.dashboard.adapter.AppointmentAdapter
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.prolificinteractive.materialcalendarview.CalendarMode

class ScheduleFragment : Fragment(), AppointmentHandler {
    companion object schedule {
        var status = false
    }

    lateinit var binding: FragmentScheduleBinding
    lateinit var model: DashboardViewModel

    lateinit var appointmentAdapter: AppointmentAdapter

    private var newAppointmentsList: List<Appointment> = listOf()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule, container, false)

        toolbarConfig()

        binding.handler = this

        model = ViewModelProvider(this, DashboardViewModelFactory())[DashboardViewModel::class.java]

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener datos del usuario
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val userName = sharedPref.getString("user_name", "")

        // Actualizar el TextView
        binding.mensageWelcome.text = "Hola $userName \n¡Reserva una cita ahora!"


        calendarSetup()
        makeAppointment()
    }

    fun calendarSetup() {
        binding.calendarView.setOnTitleClickListener(View.OnClickListener {
            if (status) {
                binding.calendarView.state().edit().setCalendarDisplayMode(CalendarMode.WEEKS)
                    .commit()
                status = false
            } else {
                binding.calendarView.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS)
                    .commit()
                status = true
            }
        })

        binding.calendarView.state().edit().isCacheCalendarPositionEnabled(true)
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.layoutHeader.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = ""
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

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
        // Usa las listas filtradas de citas según su estado
        val inProgressAdapter = AppointmentAdapter(ArrayList(model.getInProgressAppointments()), this)
        val nextConsultsAdapter = AppointmentAdapter(ArrayList(model.getNextAppointments()), this)
        val pastConsultsAdapter = AppointmentAdapter(ArrayList(model.getPastAppointments()), this)

        // Establece la lista de citas nuevas
        newAppointmentsList = model.getNewAppointmentList()  // Aquí puedes cambiar la lógica según lo que necesites

        // Asigna el adaptador para mostrar la lista de citas nuevas
        appointmentAdapter = AppointmentAdapter(newAppointmentsList, this)
        binding.recyclerviewAppointments.apply {
            adapter = appointmentAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        binding.recyclerviewInProgress.apply {
            adapter = inProgressAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        binding.recyclerviewNextConsults.apply {
            adapter = nextConsultsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        binding.recyclerviewPastConsults.apply {
            adapter = pastConsultsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun appointmentClicked(appointment: Appointment) {
        if (appointment.statusType in listOf(1, 2, 3)) {
            // Si el estado es 1, 2 o 3, navega a global_appointmentDetailFragment
            findNavController().navigate(
                R.id.global_appointmentDetailFragment,
                bundleOf("appointment" to appointment.statusType)
            )
        } else {
            // Si el estado es 4 (o cualquier otro si lo decides), no navega
            // Puedes agregar una notificación aquí si lo deseas
        }
    }

}