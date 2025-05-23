package com.ascrib.nutrifit.ui.planList

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.databinding.FragmentHistoryNutriListBinding
import com.ascrib.nutrifit.databinding.FragmentHistoryNutriListInfoBinding
import com.ascrib.nutrifit.model.Consulta
import com.ascrib.nutrifit.repository.NotificacionRepository
import com.ascrib.nutrifit.ui.dashboard.ScheduleFragment
import com.ascrib.nutrifit.ui.dashboard.ScheduleFragment.Companion
//import com.ascrib.nutrifit.ui.dashboard.adapter.AppointmentAdapter
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModel
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModelFactory
import com.ascrib.nutrifit.util.Statusbar
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class PlanListDetailFragment : Fragment() {

    private lateinit var binding: FragmentHistoryNutriListInfoBinding
    private lateinit var sharedPref: SharedPreferences
    private var consultaId: Int? = null
    private var pacienteIds: Int? = null

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
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_history_nutri_list_info,
            container,
            false
        )
        binding.handler = this
        Statusbar.setStatusbarTheme(
            requireContext(),
            requireActivity().window,
            R.color.lightGrey,
            binding.root
        )
        // Obtener la consulta pasada como argumento
        sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)

        // Obtener el ID de la consulta pasado como argumento
        consultaId = arguments?.getInt("consulta_id")
        pacienteIds = arguments?.getInt("pacienteIds")

        mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil)

        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Mostrar los datos de la consulta si existe
        consultaId?.let {
            loadConsultaById(it)
        }
        loadFotoPaciente()
        startNotificationPolling()

        // Solo consulta las notificaciones sin reproducir sonido en la primera carga
        // después de la creación de la vista
        loadNotificationCount()
    }

    private fun loadConsultaById(consultaId: Int) {
        lifecycleScope.launch {
            try {
                val pacienteIds = getPacienteIdsFromSharedPref()
                if (pacienteIds.isEmpty()) return@launch

                val response = RetrofitClient.apiService.getDetalleConsulta(
                    consultaId = consultaId,
                    pacienteIds = pacienteIds
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    response.body()?.data?.consulta?.let { consulta ->
                        loadConsultaDetails(consulta)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Manejar error adecuadamente
            }
        }
    }

    private fun getPacienteIdsFromSharedPref(): List<Int> {
        val mainPacienteId = sharedPref.getInt("Paciente_ID", 0).takeIf { it != 0 } ?: return emptyList()

        val additionalIds = try {
            sharedPref.getStringSet("paciente_ids", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        } catch (e: ClassCastException) {
            emptyList()
        }

        return (listOf(mainPacienteId) + additionalIds).distinct()
    }

    private fun loadConsultaDetails(consulta: com.ascrib.nutrifit.api.models.Consulta) {
        // Configurar los datos en las vistas
        binding.apply {
            // Datos del paciente
            if (!consulta.foto_paciente.isNullOrEmpty()) {
                Glide.with(this@PlanListDetailFragment)
                    .load(consulta.foto_paciente) // Usamos el campo foto que viene del endpoint
                    .placeholder(R.drawable.userdummy) // Imagen por defecto
                    .error(R.drawable.userdummy) // Imagen si hay error
                    .into(foto)
            }
            nombreCompleto.text = "${consulta.nombre_paciente ?: ""} ${consulta.apellidos ?: ""}"
            genero.text = consulta.genero ?: ""
            fechaNacimiento.text = formatFechaNacimiento(consulta.fecha_nacimiento)
            EstadoCiudad.text = "${consulta.ciudad ?: ""}, ${consulta.localidad ?: ""}"

            // Medidas antropométricas
            peso.text = "${consulta.peso ?: ""} kg"
            talla.text = consulta.talla ?: ""
            cintura.text = "${consulta.cintura ?: ""} cm"
            cadera.text = "${consulta.cadera ?: ""} cm"
            gc.text = "${consulta.gc ?: ""}%"
            mm.text = "${consulta.mm ?: ""} kg"
            em.text = "${consulta.em?.toDoubleOrNull()?.toInt() ?: ""} años"
            altura.text = "${consulta.altura ?: ""} m"

            // Fecha de consulta - necesitas formatear según tu respuesta JSON
            fechaConsulta.text = formatFechaConsulta(
                consulta.fecha_creacion,
                consulta.nombre_consultorio,
                consulta.nombre_nutriologo
            )

            // Diagnóstico - Limpiar HTML si es necesario
            detallesDiagnostico.text = consulta.detalles_diagnostico?.replace("<[^>]*>".toRegex(), "") ?: ""
            resultadosEvaluacion.text = consulta.resultados_evaluacion?.replace("<[^>]*>".toRegex(), "") ?: ""
            analisisNutricional.text = consulta.analisis_nutricional?.replace("<[^>]*>".toRegex(), "") ?: ""

            // Objetivos
            objetivoDescripcion.text = consulta.objetivo_descripcion?.replace("<[^>]*>".toRegex(), "") ?: ""

            // Próxima consulta - usa el campo correcto de la respuesta JSON
            proximaConsulta.text = formatProximaConsulta(consulta.proxima_consulta, consulta.nombre_nutriologo) // Ajusta según los datos disponibles

            // Mostrar mensaje según estado
            when (consulta.estado_proximaConsulta?.toIntOrNull()) {
                3 -> textConsulFinish.visibility = View.VISIBLE
                4 -> textCancel.visibility = View.VISIBLE
            }
        }
    }
    private fun getNutriologoIdsFromSharedPref(): List<Int>{
        val mainNutriologo = sharedPref.getInt("nutriologo_id",0).takeIf{ it != 0 } ?: return emptyList()
        val additionalIds = try {
            sharedPref.getStringSet("user_id_nutriologo", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        } catch (e: ClassCastException) {
            emptyList()
        }
        return (listOf(mainNutriologo) + additionalIds).distinct()
    }

    private fun loadFotoPaciente() {
        lifecycleScope.launch {
            try {
                val pacienteIds = listOf(pacienteIds ?: return@launch) // Usamos el pacienteIds que recibimos como argumento
                val nutriologoIds = getNutriologoIdsFromSharedPref()

                if (pacienteIds.isEmpty() || nutriologoIds.isEmpty()) return@launch

                val response = RetrofitClient.apiService.getConsultasPorPaciente(
                    pacienteIds = pacienteIds,
                    nutriologoIds = nutriologoIds
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val fotoPaciente = response.body()?.data?.firstOrNull()?.foto_paciente
                    fotoPaciente?.let { fotoUrl ->
                        // Cargar la foto usando Glide
                        Glide.with(this@PlanListDetailFragment)
                            .load(fotoUrl)
                            .placeholder(R.drawable.userdummy)
                            .error(R.drawable.userdummy)
                            .into(binding.foto)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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

    private fun checkForNewNotifications(newCounts: Map<Int, Int>): Boolean {
        // Caso inicial (primera carga)
        if (notificationCounts.isEmpty()) return false

        // Verificar si algún paciente tiene más notificaciones que antes
        for ((pacienteId, count) in newCounts) {
            val previousCount = notificationCounts[pacienteId] ?: 0
            if (count > previousCount) {
                return true
            }
        }
        return false
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
                    if (hasNewNotifications && !isInitialLoad) {
                        playNotificationSound()
                    }

                    // 5. Actualizar los conteos
                    notificationCounts.clear()
                    notificationCounts.putAll(newCounts)
                    lastNotificationCount = totalCount
                    isInitialLoad = false

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


    private fun formatFechaNacimiento(fecha: String?): String {
        if (fecha.isNullOrEmpty()) return ""

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(fecha)
            date?.let { outputFormat.format(it) } ?: fecha
        } catch (e: Exception) {
            e.printStackTrace()
            fecha
        }
    }

    private fun formatFechaConsulta(fechaCreacion: String?, nombreConsultorio: String?, nombreNutriologo: String?): String {
        if (fechaCreacion.isNullOrEmpty()) return ""

        return try {
            // Primero intentamos con formato fecha completa (con hora)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = inputFormat.parse(fechaCreacion)

            if (date != null) {
                val fechaFormat = SimpleDateFormat("d 'de' MMMM 'del' yyyy", Locale("es", "ES"))
                val fechaFormateada = fechaFormat.format(date)

                "$fechaFormateada\nEn $nombreConsultorio con Nut. ${nombreNutriologo ?: ""}"
            } else {
                // Si falla, intentamos solo con fecha
                val inputFormatDateOnly = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val dateOnly = inputFormatDateOnly.parse(fechaCreacion)

                if (dateOnly != null) {
                    val fechaFormat = SimpleDateFormat("d 'de' MMMM 'del' yyyy", Locale("es", "ES"))
                    val fechaFormateada = fechaFormat.format(dateOnly)

                    "$fechaFormateada\nEn $nombreConsultorio\ncon Nut. ${nombreNutriologo ?: ""}"
                } else {
                    fechaCreacion
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            fechaCreacion
        }
    }


    private fun formatProximaConsulta(proximaConsulta: String?, nombreNutriologo: String?): String {
        if (proximaConsulta.isNullOrEmpty()) return "No programada"

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val date = inputFormat.parse(proximaConsulta)

            if (date != null) {
                val fechaFormat = SimpleDateFormat("d 'de' MMMM 'del' yyyy", Locale("es", "ES"))
                val horaFormat = SimpleDateFormat("h:mm a", Locale("es", "ES"))

                val fechaFormateada = fechaFormat.format(date)
                val horaFormateada = horaFormat.format(date)

                "Próxima cita programada:\n${fechaFormateada} a las ${horaFormateada}\ncon Nut. ${nombreNutriologo ?: ""}"
            } else {
                proximaConsulta
            }
        } catch (e: Exception) {
            e.printStackTrace()
            proximaConsulta
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
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}