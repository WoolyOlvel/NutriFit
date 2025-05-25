package com.ascrib.nutrifit.ui.planList

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.icu.util.TimeZone
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.databinding.FragmentHistoryNutriListBinding
import com.ascrib.nutrifit.handler.ConsultaHandler
import com.ascrib.nutrifit.model.Consulta
import com.ascrib.nutrifit.repository.NotificacionRepository

import com.ascrib.nutrifit.ui.dashboard.adapter.ConsultaAdapter
import com.ascrib.nutrifit.ui.planList.PlanListDetailFragment.Companion
import com.ascrib.nutrifit.util.Statusbar
import com.ascrib.nutrifit.util.getStatusBarHeight
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.DateTimeException
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit

class HistoryNutriListFragment : Fragment(), ConsultaHandler {

    lateinit var binding: FragmentHistoryNutriListBinding
    private lateinit var sharedPref: SharedPreferences
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_history_nutri_list, container, false)
        binding.handler = this
        Statusbar.setStatusbarTheme(
            requireContext(),
            requireActivity().window,
            R.color.lightGrey,
            binding.root
        )
        sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)

        // Inicializar con listas vacías para evitar el error "No adapter attached"
        setupRecyclers(emptyList(), emptyList(), emptyList())
        mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil)

        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadConsultas()
        startNotificationPolling()

        // Solo consulta las notificaciones sin reproducir sonido en la primera carga
        // después de la creación de la vista
        loadNotificationCount()
    }


    private fun loadConsultas() {
        lifecycleScope.launch {
            try {
                // Obtener IDs de pacientes y nutriólogos desde SharedPreferences
                val pacienteIds = getPacienteIdsFromSharedPref()
                val nutriologoIds = getNutriologoIdsFromSharedPref()
                //val consultaIds = getConsultaIdsFromSharedPref()

                if (pacienteIds.isEmpty() || nutriologoIds.isEmpty()) return@launch

                val response = RetrofitClient.apiService.getConsultasPorPaciente(
                    pacienteIds = pacienteIds,
                    nutriologoIds = nutriologoIds
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val consultasData = response.body()?.data ?: emptyList()

                    // Mapear a objetos Consulta
                    val consultas = consultasData.map { data ->
                        val estado = data.estado_consulta?.toString()
                        val tipoConsulta = when {
                            estado == "4" -> "En Espera De Confirmación"
                            estado == "1" -> "Cita Agendada"
                            data.tipo_consulta.isNullOrEmpty() -> "No especificado"
                            else -> data.tipo_consulta
                        }
                        Consulta(
                            Reservacion_ID = data.reservacion_id,
                            Consulta_ID = data.consulta_id,
                            Paciente_ID = data.paciente_id,
                            nombre_nutriologo = data.nombre_nutriologo,
                            nombre_paciente = data.nombre_paciente,
                            estado_proximaConsulta = data.estado_consulta?.toString(),
                            fecha_consulta = data.fecha_consulta?.formatFechaConsulta(data.estado_consulta?.toString()),
                            foto = data.foto_nutriologo,
                            foto_paciente = data.foto_paciente,
                            tipo_consulta = tipoConsulta,

                        )
                    }

                    activity?.runOnUiThread {
                        (binding.recyclerviewInProgress.adapter as? ConsultaAdapter)?.updateData(
                            consultas.filter { it.estado_proximaConsulta == "4" }
                        )
                        (binding.recyclerviewNextConsults.adapter as? ConsultaAdapter)?.updateData(
                            consultas.filter { it.estado_proximaConsulta == "1" }
                        )
                        (binding.recyclerviewPastConsults.adapter as? ConsultaAdapter)?.updateData(
                            consultas.filter { it.estado_proximaConsulta == "3" }
                        )
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

    private fun getNutriologoIdsFromSharedPref(): List<Int> {
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)

        // Obtener todos los posibles IDs de nutriólogos de diferentes maneras
        val ids = mutableListOf<Int>()

        // 1. Obtener el ID principal del nutriólogo (guardado como Int)
        val mainNutriologoId = sharedPref.getInt("nutriologo_id", 0)
        if (mainNutriologoId != 0) {
            ids.add(mainNutriologoId)
        }

        // 2. Intentar obtener como Set<String>
        try {
            val nutriologoIdsSet = sharedPref.getStringSet("user_id_nutriologo", null)
            nutriologoIdsSet?.forEach {
                try {
                    ids.add(it.toInt())
                } catch (e: NumberFormatException) {
                    // Ignorar valores no numéricos
                }
            }
        } catch (e: ClassCastException) {
            // Si falla, intentar otras formas
        }

        // 3. Intentar obtener como String individual
        try {
            val singleIdStr = sharedPref.getString("user_id_nutriologo", null)
            singleIdStr?.toIntOrNull()?.let { ids.add(it) }
        } catch (e: ClassCastException) {
            // Si falla, continuar
        }

        // 4. Intentar obtener como Int individual (último recurso)
        try {
            val singleIdInt = sharedPref.getInt("user_id_nutriologo", 0)
            if (singleIdInt != 0) {
                ids.add(singleIdInt)
            }
        } catch (e: ClassCastException) {
            // Si falla, continuar
        }

        // Eliminar duplicados y devolver
        return ids.distinct().also {
            Log.d("NutriologoIds", "IDs obtenidos: $it")
        }
    }

    private fun setupRecyclers(enProgreso: List<Consulta>, proximas: List<Consulta>, realizadas: List<Consulta>) {
        // Limitar a 10 elementos por sección y ordenar descendente (más reciente primero)
        val enProgresoLimited = enProgreso
            .sortedByDescending { it.fecha_consulta }
            .take(10)

        val proximasLimited = proximas
            .sortedByDescending { it.fecha_consulta }
            .take(10)

        val realizadasLimited = realizadas
            .sortedByDescending { it.fecha_consulta }
            .take(10)

        // Consultas en progreso
        binding.recyclerviewInProgress.apply {
            adapter = ConsultaAdapter(enProgresoLimited, this@HistoryNutriListFragment)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        // Próximas consultas
        binding.recyclerviewNextConsults.apply {
            adapter = ConsultaAdapter(proximasLimited, this@HistoryNutriListFragment)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        // Consultas realizadas
        binding.recyclerviewPastConsults.apply {
            adapter = ConsultaAdapter(realizadasLimited, this@HistoryNutriListFragment)
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    @SuppressLint("NewApi")
    fun String.formatFechaConsulta(estado: String?): String {
        return try {
            // Formateadores para los diferentes patrones de fecha
            val isoFormatter = DateTimeFormatter.ISO_INSTANT
            val simpleFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

            // Parsear la fecha como LocalDateTime ignorando zonas horarias
            val consulta = try {
                LocalDateTime.ofInstant(Instant.from(isoFormatter.parse(this)), ZoneOffset.UTC)
            } catch (e: DateTimeException) {
                LocalDateTime.parse(this, simpleFormatter)
            }

            // Obtener fecha y hora actual como LocalDateTime sin zona horaria
            val ahora = LocalDateTime.now()
            val estadoNum = estado?.toIntOrNull()

            when (estadoNum) {
                1 -> { // En progreso
                    if (consulta.isBefore(ahora)) {
                        "Consulta en curso"
                    } else {
                        calcularTiempoRestante(ahora, consulta)
                    }
                }
                2, 3 -> { // Próximas o realizadas
                    DateTimeFormatter.ofPattern("dd/MM/yyyy 'a las' hh:mm a")
                        .format(consulta)
                }
                4 -> { // En espera
                    "${DateTimeFormatter.ofPattern("dd/MM/yyyy")
                        .format(consulta)} (En Espera)"
                }
                else -> this
            }
        } catch (e: Exception) {
            e.printStackTrace()
            this
        }
    }


    @SuppressLint("NewApi")
    private fun calcularTiempoRestante(ahora: LocalDateTime, consulta: LocalDateTime): String {
        val duration = Duration.between(ahora, consulta)
        val totalMinutes = duration.toMinutes()
        val days = totalMinutes / (24 * 60)
        val hours = (totalMinutes % (24 * 60)) / 60
        val minutes = totalMinutes % 60

        return when {
            days > 0 -> "Faltan $days día(s), $hours hora(s) y $minutes minuto(s) para su consulta"
            hours > 0 -> "Faltan $hours hora(s) y $minutes minuto(s) para su consulta"
            minutes > 0 -> "Faltan $minutes minuto(s) para su consulta"
            else -> "La consulta comenzará pronto"
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
        if (HistoryNutriListFragment.notificationCounts.isEmpty()) return false

        // Verificar si algún paciente tiene más notificaciones que antes
        for ((pacienteId, count) in newCounts) {
            val previousCount = HistoryNutriListFragment.notificationCounts[pacienteId] ?: 0
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
                    if (hasNewNotifications && !HistoryNutriListFragment.isInitialLoad) {
                        playNotificationSound()
                    }

                    // 5. Actualizar los conteos
                    HistoryNutriListFragment.notificationCounts.clear()
                    HistoryNutriListFragment.notificationCounts.putAll(newCounts)
                    HistoryNutriListFragment.lastNotificationCount = totalCount
                    HistoryNutriListFragment.isInitialLoad = false

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

    override fun consultaClicked(consulta: Consulta) {

        if (consulta.Consulta_ID != null && consulta.estado_proximaConsulta == "Realizado") {
            lifecycleScope.launch {
                try {
                    val pacienteIds = getPacienteIdsFromSharedPref()
                    if (pacienteIds.isEmpty()) return@launch

                    val response = RetrofitClient.apiService.getDetalleConsulta(
                        consultaId = consulta.Consulta_ID,
                        pacienteIds = pacienteIds
                    )

                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.data?.consulta?.let { detalle ->
                            // Debug: verifica los datos antes de navegar
                            println("Navegando con consulta: ${detalle.Consulta_ID}")

                            // CAMBIO: Pasar solo el ID en lugar del objeto completo
                            findNavController().navigate(
                                R.id.global_planListDetailFragment,
                                bundleOf("consulta_id" to detalle.Consulta_ID, "pacienteIds" to consulta.Paciente_ID) // Solo pasar el ID
                            )
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Muestra un error al usuario
                }
            }
        }
    }
}