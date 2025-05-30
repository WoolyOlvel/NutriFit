package com.ascrib.nutrifit.ui.dashboard

import android.content.Context
import android.content.SharedPreferences
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
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.MiProgresoResponse
import com.ascrib.nutrifit.databinding.FragmentHomeBinding
import com.ascrib.nutrifit.repository.NotificacionRepository
import com.ascrib.nutrifit.ui.form.adapter.SliderAdapter
import com.ascrib.nutrifit.util.Statusbar
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding
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
    private var lastGcValue = 0f
    private var lastMmValue = 0f

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        binding.handler = this
        Statusbar.setStatusbarTheme(
            requireContext(),
            requireActivity().window,
            R.color.lightGrey,
            binding.root
        )
        mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil)

        toolbarConfig()
        return binding.root
    }

    private val sliderHandler = Handler(Looper.getMainLooper())
    private val sliderRunnable = Runnable {
        val nextItem = binding.vpWelcome.currentItem + 1
        if (nextItem < (binding.vpWelcome.adapter?.itemCount ?: 0)) {
            binding.vpWelcome.currentItem = nextItem
        } else {
            binding.vpWelcome.currentItem = 0 // Vuelve al principio cuando llega al final
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener datos del usuario
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 0)
        val userName = sharedPref.getString("user_name", "")
        val emailUser = sharedPref.getString("user_email", "")

        // Actualizar el TextView
        binding.mensageWelcome.text = "Hola $userName ¡Bienvenido/a \nEstamos encantados de tenerte aquí!"
        if (userId != 0) {
            fetchUserProfile(userId)
        }
        emailUser?.let { email ->
            if (email.isNotEmpty()) {
                fetchPacienteData(email)
            }
        }

        fetchProgressData() // Llamar a la nueva función

        val pagerAdapter = SliderAdapter(context as FragmentActivity)
        binding.vpWelcome.adapter = pagerAdapter
        binding.dotsIndicator.setViewPager2(binding.vpWelcome)

        // escuchador de página para reiniciar el temporizador cuando el usuario desliza manualmente
        binding.vpWelcome.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Cuando el usuario cambia de página, reinicia el temporizador
                sliderHandler.removeCallbacks(sliderRunnable)
                sliderHandler.postDelayed(sliderRunnable, 3000) // 3 segundos
            }
        })

        // Inicia el desplazamiento automático
        startAutoSlide()

        // Cargar contador de notificaciones
        startNotificationPolling()
        loadNotificationCount()
        // Deshabilitar funcionalidad del SearchView
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Toast.makeText(requireContext(), "El buscador se encuentra en mantenimiento", Toast.LENGTH_SHORT).show()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return true
            }
        })
    }

    // FUNCIONES PARA OBTENER IDs - USAR ESTAS EN LUGAR DE LAS DUPLICADAS
    private fun getPacienteIdsFromSharedPref(): List<Int> {
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
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

        return ids.distinct().also {
            Log.d("NutriologoIds", "IDs obtenidos: $it")
        }
    }

    private fun fetchProgressData() {
        val pacienteIds = getPacienteIdsFromSharedPref()
        val nutriologoIds = getNutriologoIdsFromSharedPref()

        Log.d("ProgressData", "Paciente IDs: $pacienteIds, Nutriologo IDs: $nutriologoIds")

        if (pacienteIds.isEmpty() || nutriologoIds.isEmpty()) {
            Log.e("ProgressData", "IDs no válidos - Pacientes: $pacienteIds, Nutriologos: $nutriologoIds")
            binding.progressView.setProgress(0, true)
            return
        }

        // Primero obtenemos las consultas para luego obtener sus IDs
        lifecycleScope.launch {
            try {
                Log.d("ProgressData", "Obteniendo consultas...")

                val responseConsultas = withContext(Dispatchers.IO) {
                    RetrofitClient.apiService.getConsultasPorPaciente3(
                        pacienteIds = pacienteIds,
                        nutriologoIds = nutriologoIds
                    )
                }

                if (responseConsultas.isSuccessful && responseConsultas.body()?.success == true) {
                    val consultas = responseConsultas.body()?.data ?: emptyList()
                    val consultaIds = consultas.map { it.consulta_id }

                    Log.d("ProgressData", "Consulta IDs obtenidos: $consultaIds")

                    if (consultaIds.isEmpty()) {
                        Log.w("ProgressData", "No hay consultas disponibles")
                        binding.progressView.setProgress(0, true)
                        return@launch
                    }

                    // Ahora obtenemos los datos de GC y MM
                    val responseGcMm = withContext(Dispatchers.IO) {
                        RetrofitClient.apiService.getGcMmPorConsulta(
                            pacienteIds = pacienteIds,
                            nutriologoIds = nutriologoIds,
                            consultaIds = consultaIds
                        )
                    }

                    Log.d("ProgressData", "Respuesta GC/MM recibida - Código: ${responseGcMm.code()}")

                    if (responseGcMm.isSuccessful) {
                        responseGcMm.body()?.let { progresoResponse ->
                            Log.d("ProgressData", "Body recibido: $progresoResponse")

                            if (progresoResponse.success) {
                                Log.d("ProgressData", "Datos recibidos: ${progresoResponse.data}")
                                calculateAndUpdateProgress(progresoResponse)
                            } else {
                                Log.e("ProgressData", "Respuesta no exitosa del servidor")
                                binding.progressView.setProgress(0, true)
                            }
                        } ?: run {
                            Log.e("ProgressData", "Body de respuesta es null")
                            binding.progressView.setProgress(0, true)
                        }
                    } else {
                        val errorBody = responseGcMm.errorBody()?.string()
                        Log.e("ProgressData", "Error en la respuesta: ${responseGcMm.code()} - $errorBody")
                        binding.progressView.setProgress(0, true)
                    }
                } else {
                    Log.e("ProgressData", "Error al obtener consultas")
                    binding.progressView.setProgress(0, true)
                }
            } catch (e: Exception) {
                Log.e("ProgressData", "Excepción al obtener datos: ${e.message}", e)
                binding.progressView.setProgress(0, true)
            }
        }
    }

    // FUNCIONES FALTANTES PARA EL CÁLCULO DE PROGRESO
    private fun parseFloatValue(value: String?, fieldName: String): Float {
        return try {
            if (value.isNullOrBlank()) {
                Log.w("ProgressData", "$fieldName está vacío o nulo")
                0f
            } else {
                // Limpiar el valor eliminando caracteres no numéricos excepto punto y coma
                val cleanValue = value.replace(Regex("[^0-9.,]"), "")
                    .replace(",", ".") // Convertir comas a puntos para decimales

                if (cleanValue.isBlank()) {
                    Log.w("ProgressData", "$fieldName no contiene valores numéricos válidos: '$value'")
                    0f
                } else {
                    val floatValue = cleanValue.toFloat()
                    Log.d("ProgressData", "$fieldName: '$value' -> $floatValue")
                    floatValue
                }
            }
        } catch (e: NumberFormatException) {
            Log.e("ProgressData", "Error al parsear $fieldName: '$value' - ${e.message}")
            0f
        }
    }

    private fun calculateProgressValue(gcValue: Float, mmValue: Float): Int {
        return try {
            Log.d("ProgressData", "Calculando progreso con GC: $gcValue, MM: $mmValue")

            // Si ambos valores son 0, retornar 0
            if (gcValue == 0f && mmValue == 0f) {
                Log.w("ProgressData", "Ambos valores son 0")
                return 0
            }

            // Calcular la diferencia (masa muscular - grasa corporal)
            val diferencia = mmValue - gcValue
            Log.d("ProgressData", "Diferencia (MM - GC): $diferencia")

            // Normalizar a un rango de 0-100
            val progressValue = when {
                diferencia > 0 -> {
                    // Si la masa muscular es mayor que la grasa
                    // Mapear a un rango de 50-100 (ejemplo: diferencia de 0 = 50%, +10 = 100%)
                    (50 + (diferencia * 5)).coerceAtMost(100f).toInt()
                }
                diferencia < 0 -> {
                    // Si la grasa es mayor que la masa muscular
                    // Mapear a un rango de 0-50 (ejemplo: diferencia de 0 = 50%, -10 = 0%)
                    (50 + (diferencia * 5)).coerceAtLeast(0f).toInt()
                }
                else -> 50 // Si son iguales
            }

            Log.d("ProgressData", "Progreso final calculado: $progressValue")
            progressValue

        } catch (e: Exception) {
            Log.e("ProgressData", "Error al calcular progreso: ${e.message}", e)
            0 // Valor por defecto en caso de error
        }
    }

    private fun calculateAndUpdateProgress(progresoData: MiProgresoResponse) {
        Log.d("ProgressData", "Calculando progreso...")

        // Obtener todas las consultas
        val allConsultas = progresoData.data.flatMap { pacienteData ->
            Log.d("ProgressData", "Paciente con ${pacienteData.consultas.size} consultas")
            pacienteData.consultas
        }

        Log.d("ProgressData", "Total de consultas encontradas: ${allConsultas.size}")

        if (allConsultas.isEmpty()) {
            Log.w("ProgressData", "No hay consultas disponibles")
            binding.progressView.setProgress(0, true)
            return
        }

        // Obtener la consulta más reciente (asumiendo que consulta_id es incremental)
        val latestConsulta = allConsultas.maxByOrNull { it.consulta_id } ?: allConsultas.last()

        Log.d("ProgressData", "Consulta más reciente ID: ${latestConsulta.consulta_id}")
        Log.d("ProgressData", "GC: '${latestConsulta.gc}', MM: '${latestConsulta.mm}'")

        // Procesar valores con mejor manejo de errores
        val gcValue = parseFloatValue(latestConsulta.gc, "GC")
        val mmValue = parseFloatValue(latestConsulta.mm, "MM")

        Log.d("ProgressData", "Valores procesados - GC: $gcValue, MM: $mmValue")

        // Calcular el progreso
        val progressValue = calculateProgressValue(gcValue, mmValue)

        Log.d("ProgressData", "Progreso calculado: $progressValue%")

        // Actualizar el CircularProgressView en el hilo principal
        activity?.runOnUiThread {
            try {
                binding.progressView.setProgress(progressValue, true)
                Log.d("ProgressData", "Progress actualizado en la UI: $progressValue%")
            } catch (e: Exception) {
                Log.e("ProgressData", "Error al actualizar UI: ${e.message}", e)
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
        // Usar la función principal que ya tienes
        return getPacienteIdsFromSharedPref()
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

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberar recursos del MediaPlayer
        stopNotificationPolling()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    private fun fetchPacienteData(email: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPacienteByEmail(email)
                if (response.isSuccessful) {
                    val paciente = response.body()?.paciente
                    paciente?.let {
                        // Cargar la foto del paciente con Glide
                        it.foto?.let { fotoUrl ->
                            Glide.with(requireContext())
                                .load(fotoUrl)
                                .placeholder(R.drawable.userdummy) // Imagen por defecto
                                .error(R.drawable.usererrror) // Imagen si hay error
                                .into(binding.foto)
                        }
                    }
                } else {
                    // Manejar error de respuesta
                    val errorBody = response.errorBody()?.string()
                    // Log.e("ProfileFragment", "Error fetching paciente: $errorBody")
                }
            } catch (e: Exception) {
                // Manejar excepciones
                // Log.e("ProfileFragment", "Error: ${e.message}")
            }
        }
    }

    private fun fetchUserProfile(userId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getProfileUser(userId)
                if (response.isSuccessful) {
                    val userProfile = response.body()?.user
                    userProfile?.let { user ->
                        // Actualizar la UI con los datos frescos
                        binding.mensageWelcome.text = "Hola ${user.nombre} ¡Bienvenido/a \nEstamos encantados de tenerte aquí!"

                        // Opcional: Actualizar SharedPreferences con los nuevos datos
                        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("user_name", user.nombre)
                            apply()
                        }
                    }
                } else {
                    // Manejar error de respuesta
                    val errorBody = response.errorBody()?.string()
                    // Log.e("HomeFragment", "Error fetching profile: $errorBody")
                }
            } catch (e: Exception) {
                // Manejar excepciones
                // Log.e("HomeFragment", "Error: ${e.message}")
            }
        }
    }

    // Método para iniciar el desplazamiento automático
    private fun startAutoSlide() {
        sliderHandler.postDelayed(sliderRunnable, 3000) // 3 segundos
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
        stopNotificationPolling()
    }

    // Reinicia el desplazamiento automático cuando el fragmento vuelve a ser visible
    override fun onResume() {
        super.onResume()
        startAutoSlide()

        if (!isPollingActive) {
            startNotificationPolling()
        }
        // Carga inmediata al volver
        loadNotificationCount()
        fetchProgressData()
    }

    fun onSearchClicked() {
        AlertDialog.Builder(requireContext())
            .setTitle("Buscador no disponible")
            .setMessage("Estamos trabajando para mejorar esta función. Estará disponible en futuras actualizaciones.")
            .setPositiveButton("Entendido", null)
            .show()
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
                // Configuración segura del badge
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

    fun onMyProgessClicked() {
        findNavController().navigate(R.id.action_homeFragment_a_myProgressFragment)
    }

    fun onPlanAlimentClicked() {
        findNavController().navigate(R.id.global_planListFragment)
    }

    fun onNutriDefOnClicked() {
        findNavController().navigate(R.id.global_desafioNutriFragment)
    }

    fun onReportSaludClicked() {
        findNavController().navigate(R.id.action_homeFragment_a_myPersonSaludFragment)
    }

    fun onHistorialNutriClicked() {
        findNavController().navigate(R.id.action_homeFragment_a_historyNutriListFragment)
    }
}