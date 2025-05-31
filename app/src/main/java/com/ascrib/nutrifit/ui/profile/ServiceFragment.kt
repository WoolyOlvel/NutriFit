package com.ascrib.nutrifit.ui.profile

import android.app.TimePickerDialog
import android.content.ContentValues.TAG
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.CheckBox
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
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
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.OptIn
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.Paciente
import com.ascrib.nutrifit.api.models.TipoConsulta
import com.ascrib.nutrifit.api.models.UpdatePacienteRequest
import com.ascrib.nutrifit.repository.NotificacionRepository
import com.ascrib.nutrifit.ui.dashboard.ProfileFragment
import com.ascrib.nutrifit.util.Statusbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ServiceFragment : Fragment(), OnDateSelectedListener {
    lateinit var binding: FragmentServicesBinding
    private var selectedDate: CalendarDay? = null
    private var selectedHour: Int = 0
    private var selectedMinute: Int = 0
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    // Lista para almacenar los pares de checkbox y card
    private var checkboxCardPairs = mutableListOf<Pair<CheckBox, CardView>>()
    private var nutriologoId: Int = 0
    private var selectedConsultaType: String? = null
    private var selectedPrecio: Double = 0.0
    private var selectedMotivo: String = ""
    companion object {
        var status = false
        private var lastNotificationCount = 0
        private var isInitialLoad = true

        // Nueva lista para mantener los conteos por paciente
        private val notificationCounts = mutableMapOf<Int, Int>()
    }
    private val notificacionRepository = NotificacionRepository()
    private val notificationHandler = Handler(Looper.getMainLooper())
    private var notificationRunnable: Runnable? = null
    private val pollingInterval = 15000L // 15 segundos
    private var isPollingActive = false
    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_services, container, false)

        binding.handler = this
        Statusbar.setStatusbarTheme(
            requireContext(),
            requireActivity().window,
            R.color.lightGrey,
            binding.root
        )
        mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil)

        toolbarConfig()
        setupCalendar()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        nutriologoId = arguments?.getInt("nutriologo_id", 0) ?: 0
        loadTiposConsulta()
        setupCheckboxListeners()
        setupTextWatchers()
        startNotificationPolling()
        loadNotificationCount()
    }


    private fun loadTiposConsulta() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getTiposConsulta()
                if (response.isSuccessful) {
                    response.body()?.data?.let { tiposConsulta ->
                        updatePrecios(tiposConsulta)
                    }
                } else {
                    // Manejar error
                    Toast.makeText(context, "Error al cargar precios", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Manejar excepción
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun formatPrecio(valor: Double): String {
        return String.format(Locale.getDefault(), "$%.2f MXN", valor)
    }


    private fun updatePrecios(tiposConsulta: List<TipoConsulta>) {
        tiposConsulta.forEach { tipo ->
            when (tipo.Nombre) {
                "Consulta Por App" -> {
                    binding.totalPagoConsultaPorApp.setText(formatPrecio(tipo.total_pago))
                }

                "Consulta Por Llamada" -> {
                    binding.totalPagoConsultaPorLlamada.setText(formatPrecio(tipo.total_pago))

                }

                "Consulta Normal" -> {
                    binding.totalPagoConsultaNormal.setText(formatPrecio(tipo.total_pago))
                }

            }
        }
    }

    private fun setupCheckboxListeners() {
        // Configurar pares checkbox-card
        checkboxCardPairs = listOf(
            Pair(binding.checkboxSendMessage, binding.cardConsultaPorApp),
            Pair(binding.checkboxSendMessage2, binding.cardConsultaNormal),
            Pair(binding.checkboxPersonalCall, binding.cardConsultaPorLlamada)
        ).toMutableList()

        // Configurar listeners
        binding.checkboxSendMessage.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedConsultaType = "Consulta Por App"
                selectedPrecio = binding.totalPagoConsultaPorApp.text.toString()
                    .replace("$", "").replace(" MXN", "").toDouble()
                selectedMotivo = binding.motivoConsultaApp.text.toString()
                hideOtherCards(binding.checkboxSendMessage)
            }
        }

        binding.checkboxSendMessage2.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedConsultaType = "Consulta Normal"
                selectedPrecio = binding.totalPagoConsultaNormal.text.toString()
                    .replace("$", "").replace(" MXN", "").toDouble()
                selectedMotivo = binding.motivoConsultaPresencial.text.toString()
                hideOtherCards(binding.checkboxSendMessage2)
            }
        }

        binding.checkboxPersonalCall.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedConsultaType = "Consulta Por Llamada"
                selectedPrecio = binding.totalPagoConsultaPorLlamada.text.toString()
                    .replace("$", "").replace(" MXN", "").toDouble()
                selectedMotivo = binding.motivoConsultaLlamada.text.toString()
                hideOtherCards(binding.checkboxPersonalCall)
            }
        }




    }

    private fun hideOtherCards(selectedCheckbox: CheckBox) {
        checkboxCardPairs.forEach { (checkbox, card) ->
            if (checkbox != selectedCheckbox) {
                card.isVisible = false
                checkbox.setOnCheckedChangeListener(null)
                checkbox.isChecked = false
                checkbox.setOnCheckedChangeListener { buttonView, isChecked ->
                    if (isChecked) {
                        hideOtherCards(buttonView as CheckBox)
                    }
                }
            } else {
                card.isVisible = true
            }
        }
    }

    private fun getFormattedDateTime(): String {
        return if (selectedDate != null) {
            val calendar = Calendar.getInstance().apply {
                set(selectedDate!!.year, selectedDate!!.month - 1, selectedDate!!.day,
                    selectedHour, selectedMinute)
            }
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(calendar.time)
        } else {
            ""
        }
    }

    private fun showAllCards() {
        for (pair in checkboxCardPairs) {
            // Mostrar todos los cards
            pair.second.isVisible = true
        }
    }

    // Decorador para deshabilitar fines de semana (sábado y domingo)
    inner class WeekendDisableDecorator : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            val calendar = Calendar.getInstance()
            calendar.set(day.year, day.month - 1, day.day)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            // Calendar.SATURDAY = 7, Calendar.SUNDAY = 1
            return dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY
        }

        override fun decorate(view: DayViewFacade) {
            view.setDaysDisabled(true)
            // Opcional: cambiar el color para indicar que están deshabilitados
            view.addSpan(android.text.style.ForegroundColorSpan(
                ContextCompat.getColor(requireContext(), android.R.color.darker_gray)
            ))
        }
    }

    //  función setupCalendar()
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

            // NUEVO: Deshabilitar fines de semana
            addDecorator(WeekendDisableDecorator())
        }

        // Seleccionar la fecha actual y mostrarla (solo si es día de semana)
        val calendar = Calendar.getInstance()
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        if (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY) {
            selectedDate = today
            updateSelectedDateTimeDisplay()
        } else {
            // Si hoy es fin de semana, no seleccionar ninguna fecha
            selectedDate = null
            binding.tvSelectedDatetime.text = "Selecciona una fecha (Lunes a Viernes)"
        }
    }

    // También modifica tu función onDateSelected para agregar validación adicional:
    override fun onDateSelected(
        widget: MaterialCalendarView,
        date: CalendarDay,
        selected: Boolean
    ) {
        if (selected) {
            // Verificar que no sea fin de semana
            val calendar = Calendar.getInstance()
            calendar.set(date.year, date.month - 1, date.day)
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

            if (dayOfWeek == Calendar.SATURDAY || dayOfWeek == Calendar.SUNDAY) {
                // Deseleccionar si es fin de semana
                widget.setDateSelected(date, false)
                Toast.makeText(context, "Solo se pueden seleccionar días de lunes a viernes", Toast.LENGTH_SHORT).show()
                return
            }

            selectedDate = date
            showTimePickerDialog()
        }
    }


    private fun showTimePickerDialog() {
        val calendar = Calendar.getInstance()
        // Inicializar con hora actual o con hora dentro del rango permitido
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
        val initialHour = when {
            currentHour < 8 -> 8     // Si es antes de las 8:00, inicializar con 8:00
            currentHour > 19 -> 19   // Si es después de las 19:00, inicializar con 19:00
            else -> currentHour      // De lo contrario, usar la hora actual
        }
        val initialMinute = if (currentHour < 8 || currentHour > 19) 0 else calendar.get(Calendar.MINUTE)

        // Crear un TimePickerDialog personalizado que limite las horas seleccionables
        val timePickerDialog = object : TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                selectedHour = hourOfDay
                selectedMinute = minute
                updateSelectedDateTimeDisplay()
            },
            initialHour,
            initialMinute,
            true // formato 24 horas
        ) {
            // Sobrescribir el método onTimeChanged para limitar las horas seleccionables
            override fun onTimeChanged(view: android.widget.TimePicker?, hourOfDay: Int, minute: Int) {
                super.onTimeChanged(view, hourOfDay, minute)

                // Deshabilitar botón positivo si la hora está fuera del rango permitido
                val isValidTime = (hourOfDay in 8..18) || (hourOfDay == 19 && minute == 0)
                getButton(TimePickerDialog.BUTTON_POSITIVE)?.isEnabled = isValidTime

                // Mostrar mensaje informativo si la hora está fuera del rango
                view?.let {
                    if (!isValidTime) {
                        view.contentDescription = "Hora fuera del horario permitido (8:00-19:00)"
                    } else {
                        view.contentDescription = ""
                    }
                }
            }
        }

        // Mostrar el diálogo
        timePickerDialog.show()

        // Agregar listener para cuando se muestre el diálogo
        timePickerDialog.setOnShowListener {
            // Verificar la hora inicial
            val initialIsValid = (initialHour in 8..18) || (initialHour == 19 && initialMinute == 0)
            timePickerDialog.getButton(TimePickerDialog.BUTTON_POSITIVE)?.isEnabled = initialIsValid
        }
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
                menu.findItem(R.id.action_notification)?.let { menuItem ->
                    NotificationBadgeUtils.setupBadge(requireActivity() as AppCompatActivity, menuItem)
                }
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

    private fun setupTextWatchers() {
        binding.motivoConsultaApp.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.checkboxSendMessage.isChecked) {
                    selectedMotivo = s.toString()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.motivoConsultaPresencial.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.checkboxSendMessage2.isChecked) {
                    selectedMotivo = s.toString()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.motivoConsultaLlamada.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (binding.checkboxPersonalCall.isChecked) {
                    selectedMotivo = s.toString()
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }


    @OptIn(UnstableApi::class)
    fun saveAccount() {
        binding.checkboxSendMessage.isChecked = binding.checkboxSendMessage.isChecked
        binding.checkboxSendMessage2.isChecked = binding.checkboxSendMessage2.isChecked
        binding.checkboxPersonalCall.isChecked = binding.checkboxPersonalCall.isChecked
        when {
            binding.checkboxSendMessage.isChecked -> {
                selectedMotivo = binding.motivoConsultaApp.text.toString()
            }
            binding.checkboxSendMessage2.isChecked -> {
                selectedMotivo = binding.motivoConsultaPresencial.text.toString()
            }
            binding.checkboxPersonalCall.isChecked -> {
                selectedMotivo = binding.motivoConsultaLlamada.text.toString()
            }
        }
        // Validar hora seleccionada
        Log.d("ServiceFragment", "Tipo consulta seleccionado: $selectedConsultaType")
        Log.d("ServiceFragment", "Motivo actual: $selectedMotivo")
        Log.d("ServiceFragment", "Checkbox App: ${binding.checkboxSendMessage.isChecked}")
        Log.d("ServiceFragment", "Checkbox Normal: ${binding.checkboxSendMessage2.isChecked}")
        Log.d("ServiceFragment", "Checkbox Llamada: ${binding.checkboxPersonalCall.isChecked}")
        if (selectedHour < 8 || selectedHour > 19) {
            Toast.makeText(context, "El horario debe ser entre 8:00 y 19:00", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar que se haya seleccionado un tipo de consulta
        if (selectedConsultaType.isNullOrEmpty()) {
            Toast.makeText(context, "Selecciona un tipo de consulta", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar motivo de consulta
        if (selectedMotivo.isEmpty()) {
            Toast.makeText(context, "Ingresa el motivo de la consulta", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                val email = sharedPref.getString("user_email", "") ?: ""

                // 1. Obtener paciente actual
                val pacienteResponse = RetrofitClient.apiService.getPacienteByEmail(email)
                if (!pacienteResponse.isSuccessful || pacienteResponse.body()?.paciente == null) {
                    Toast.makeText(context, "Error al obtener datos del paciente", Toast.LENGTH_SHORT).show()
                    return@launch
                }
                val paciente = pacienteResponse.body()!!.paciente

                // 2. Verificar si ya tiene reservaciones con otro nutriólogo
                val currentNutriologoId = paciente.user_id
                if (currentNutriologoId != null && currentNutriologoId != 0 && currentNutriologoId != nutriologoId) {
                    // Mostrar diálogo de confirmación
                    withContext(Dispatchers.Main) {
                        showChangeNutriologoDialog(paciente, email)
                    }
                } else {
                    // Proceder directamente con la reservación
                    proceedWithReservation(paciente, email)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e(TAG, "Error en lifecycleScope:", e)

            }
        }
    }

    private fun showChangeNutriologoDialog(paciente: Paciente, email: String) {
        if (!isAdded) return

        android.app.AlertDialog.Builder(requireContext())
            .setTitle("¿Continuar con otro nutriólogo?")
            .setMessage("Ya tienes una reservación con otro nutriólogo. ¿Deseas continuar con este nuevo nutriólogo?")
            .setPositiveButton("Sí") { _, _ ->
                // Usar viewLifecycleOwner para asegurar que la corrutina se cancele si el fragmento se destruye
                viewLifecycleOwner.lifecycleScope.launch {
                    duplicatePacienteForNewNutriologo(paciente, email)
                }
            }
            .setNegativeButton("No", null)
            .setOnDismissListener {
                // Limpiar recursos si es necesario
            }
            .show()
    }

    @OptIn(UnstableApi::class)
    private suspend fun duplicatePacienteForNewNutriologo(paciente: Paciente, email: String) {
        try {
            // Verificar si el fragmento está adjunto y visible
            if (!isAdded || !isVisible || context == null) return


            // 1. Duplicar paciente para el nuevo nutriólogo
            val duplicateResponse = RetrofitClient.apiService.duplicarPaciente(
                email = email,
                userIdNutriologo = nutriologoId
            )

            if (!duplicateResponse.isSuccessful || duplicateResponse.body()?.paciente == null) {
                withContext(Dispatchers.Main) {
                    if (isAdded && isVisible && context != null) {
                        Toast.makeText(requireContext(), "Error al duplicar paciente", Toast.LENGTH_SHORT).show()
                    }
                }
                return
            }

            val nuevoPaciente = duplicateResponse.body()!!.paciente

            // 2. Actualizar SharedPreferences con la lista de nutriólogo
            updateNutriologosListInPrefs(nutriologoId, nuevoPaciente.Paciente_ID ?: 0)
            proceedWithReservation(nuevoPaciente, email)
            // 3. Proceder con la reservación
            withContext(Dispatchers.Main) {
                if (isAdded) {
                    proceedWithReservation(nuevoPaciente, email)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                if (isAdded && isVisible && context != null) {
                    Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_SHORT)
                        .show()
                }
            }
            Log.e(TAG, "Error en duplicatePacienteForNewNutriologo()", e)
        }
    }

    @OptIn(UnstableApi::class)
    private suspend fun proceedWithReservation(paciente: Paciente, email: String) {
        try {
            // Obtener datos del nutriólogo
            val nutriologoResponse = RetrofitClient.apiService.getNutriologoById(nutriologoId)
            if (!nutriologoResponse.isSuccessful || nutriologoResponse.body()?.data == null) {
                Toast.makeText(context, "Error al obtener datos del nutriólogo", Toast.LENGTH_SHORT).show()
                return
            }
            val nutriologo = nutriologoResponse.body()!!.data

            // Crear reservación
            val reservacionResponse = RetrofitClient.apiService.createReservacion(
                pacienteId = paciente.Paciente_ID ?: 0,
                nombrePaciente = paciente.nombre,
                apellidos = paciente.apellidos,
                telefono = paciente.telefono,
                genero = paciente.genero,
                usuario = paciente.usuario,
                edad = paciente.edad,
                precioCita = selectedPrecio,
                motivoConsulta = selectedMotivo,
                nombreNutriologo = "${nutriologo.nombre_nutriologo} ${nutriologo.apellido_nutriologo}",
                fechaConsulta = getFormattedDateTime(),
                origen = "movil",
                estadoProximaConsulta = 4,
                userId = nutriologoId
            )

            if (reservacionResponse.isSuccessful) {
                Toast.makeText(context, "Cita generada exitosamente", Toast.LENGTH_SHORT).show()
                // Clear any pending operations
                stopNotificationPolling()
                // Navigate up
                findNavController().popBackStack()
            } else {
                Toast.makeText(context, "Error al crear reservación", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()

            Log.e(TAG, "Error en saveAccount()", e)
            // 3) (Opcional) También puedes imprimir en consola directa
            e.printStackTrace()
        }
    }

    private fun updateNutriologosListInPrefs(newNutriologoId: Int, nuevoPacienteId: Int) {
        val prefs = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val editor = prefs.edit()

        // Guardar lista de pacientes (esto es lo nuevo)
        val pacienteIds = try {
            prefs.getStringSet("paciente_ids", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        } catch (e: ClassCastException) {
            mutableSetOf<String>()
        }
        pacienteIds.add(nuevoPacienteId.toString())
        editor.putStringSet("paciente_ids", pacienteIds)

        // Guardar lista de nutriólogos
        val nutriologoIds = try {
            prefs.getStringSet("user_id_nutriologo", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        } catch (e: ClassCastException) {
            mutableSetOf<String>()
        }
        nutriologoIds.add(newNutriologoId.toString())
        editor.putStringSet("user_id_nutriologo", nutriologoIds)

        editor.apply()
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

    private fun startNotificationPolling() {
        if (isPollingActive) return

        isPollingActive = true
        notificationRunnable = object : Runnable {
            override fun run() {
                loadNotificationCount()
                notificationHandler.postDelayed(this, pollingInterval)
            }
        }
        notificationHandler.post(notificationRunnable!!)
    }

    private fun stopNotificationPolling() {
        isPollingActive = false
        notificationRunnable?.let {
            notificationHandler.removeCallbacks(it)
        }
    }

    private fun checkForNewNotifications(newCounts: Map<Int, Int>): Boolean {
        // Caso inicial (primera carga)
        if (ServiceFragment.notificationCounts.isEmpty()) return false

        // Verificar si algún paciente tiene más notificaciones que antes
        for ((pacienteId, count) in newCounts) {
            val previousCount = ServiceFragment.notificationCounts[pacienteId] ?: 0
            if (count > previousCount) {
                return true
            }
        }
        return false
    }

    private fun getAllPacienteIds(sharedPref: SharedPreferences): List<Int> {
        // 1. Obtener el ID principal
        val mainPacienteId = sharedPref.getInt("Paciente_ID", 0).takeIf { it != 0 } ?: return emptyList()

        // 2. Obtener IDs adicionales de SharedPreferences
        val additionalIds = try {
            sharedPref.getStringSet("paciente_ids", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        } catch (e: ClassCastException) {
            emptyList()
        }

        // 3. Combinar y eliminar duplicados
        return (listOf(mainPacienteId) + additionalIds).distinct()
    }

    private fun loadNotificationCount() {
        if (!isAdded || activity == null) return

        lifecycleScope.launch {
            try {
                val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)

                // 1. Obtener todos los IDs de paciente
                val pacienteIds = getAllPacienteIds(sharedPref)
                if (pacienteIds.isEmpty()) return@launch

                var totalCount = 0
                val newCounts = mutableMapOf<Int, Int>()

                // 2. Contar notificaciones para cada paciente
                for (pacienteId in pacienteIds) {
                    val count = withContext(Dispatchers.IO) {
                        notificacionRepository.contarNotificacionesNoLeidas(pacienteId)
                    }
                    newCounts[pacienteId] = count
                    totalCount += count
                }

                activity?.runOnUiThread {
                    // 3. Verificar si hay nuevas notificaciones
                    val hasNewNotifications = checkForNewNotifications(newCounts)

                    // 4. Reproducir sonido si hay nuevas notificaciones
                    if (hasNewNotifications && !ServiceFragment.isInitialLoad) {
                        playNotificationSound()
                    }

                    // 5. Actualizar los conteos
                    ServiceFragment.notificationCounts.clear()
                    ServiceFragment.notificationCounts.putAll(newCounts)
                    ServiceFragment.lastNotificationCount = totalCount
                    ServiceFragment.isInitialLoad = false

                    // 6. Actualizar el badge
                    NotificationBadgeUtils.updateBadgeCount(totalCount)
                    requireActivity().invalidateOptionsMenu()
                }
            } catch (e: Exception) {
                // Reintentar más rápido si hay error
                if (isPollingActive) {
                    notificationHandler.postDelayed({ loadNotificationCount() }, 5000)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberar recursos del MediaPlayer
        stopNotificationPolling()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        stopNotificationPolling()
    }

    override fun onResume() {
        super.onResume()
        if (!isPollingActive) {
            startNotificationPolling()
        }

        // Carga inmediata de notificaciones cuando vuelve el fragmento
        loadNotificationCount()
    }

    private fun playNotificationSound() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil).apply {
                    setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                    }
                }
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}