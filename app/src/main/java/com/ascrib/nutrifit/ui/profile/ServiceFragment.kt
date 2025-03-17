package com.ascrib.nutrifit.ui.profile

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentServicesBinding
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.DayViewDecorator
import com.prolificinteractive.materialcalendarview.DayViewFacade
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener
import java.text.SimpleDateFormat
import java.util.*

class ServiceFragment : Fragment(), OnDateSelectedListener {
    lateinit var binding: FragmentServicesBinding
    private var selectedDate: CalendarDay? = null
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_services, container, false)

        binding.handler = this
        toolbarConfig()
        setupCalendar()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    private fun setupCalendar() {
        val today = CalendarDay.today()
        binding.calendarView.apply {
            // Configurar fecha actual por defecto
            setSelectedDate(today)
            setCurrentDate(today)

            // Configurar el modo de selección individual
            selectionMode = MaterialCalendarView.SELECTION_MODE_SINGLE

            // Agregar listener de selección de fecha
            setOnDateChangedListener(this@ServiceFragment)

            // Deshabilitar fechas anteriores
            addDecorator(PastDayDisableDecorator(today))
        }

        // Seleccionar la fecha actual y mostrarla
        selectedDate = today
        updateSelectedDateTimeDisplay()
    }

    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        if (selected) {
            selectedDate = date
            showTimePickerDialog()
        }
    }

    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                updateSelectedDateTimeDisplay()
            },
            hour,
            minute,
            true // formato 24 horas
        ).show()
    }

    private fun updateSelectedDateTimeDisplay() {
        selectedDate?.let { date ->
            val calendar = Calendar.getInstance()
            calendar.set(date.year, date.month - 1, date.day, selectedHour, selectedMinute)

            val dateStr = dateFormat.format(calendar.time)
            val timeStr = timeFormat.format(calendar.time)

            binding.tvSelectedDatetime.text = "Fecha: $dateStr - Hora: $timeStr"
        }
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Reservarción De Cita"
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

                    R.id.action_notification -> {
                        findNavController().navigate(R.id.global_notificationFragment)
                        return true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun saveAccount() {
        findNavController().navigateUp()
    }

    // Clase para deshabilitar fechas pasadas
    inner class PastDayDisableDecorator(private val today: CalendarDay) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return day.isBefore(today)
        }

        override fun decorate(view: DayViewFacade) {
            view.setDaysDisabled(true)
        }
    }
}