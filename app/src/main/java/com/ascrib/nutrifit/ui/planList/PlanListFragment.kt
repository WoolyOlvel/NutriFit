package com.ascrib.nutrifit.ui.planList

import android.app.DownloadManager
import android.content.Context
import android.os.Bundle
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
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentPlanAlimenticioBinding
import com.ascrib.nutrifit.handler.ListHandler
import com.ascrib.nutrifit.handler.PlanAlimenticioHandler
import com.ascrib.nutrifit.model.PlanAlimenticio
import com.ascrib.nutrifit.model.PlanList
import com.ascrib.nutrifit.ui.dashboard.adapter.PlanAlimenticioAdapter
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModel
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModelFactory
import com.ascrib.nutrifit.util.getStatusBarHeight
import kotlinx.coroutines.launch
import android.content.SharedPreferences
import android.media.MediaPlayer
import android.net.Uri
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.repository.NotificacionRepository
import com.ascrib.nutrifit.ui.patient.AppointmentDetailFragment
import com.ascrib.nutrifit.ui.patient.AppointmentDetailFragment.Companion
import com.ascrib.nutrifit.util.Statusbar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class PlanListFragment : Fragment(), PlanAlimenticioHandler {

    private lateinit var binding: FragmentPlanAlimenticioBinding
    private lateinit var adapter: PlanAlimenticioAdapter
    private val scope = CoroutineScope(Dispatchers.IO)
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_plan_alimenticio, container, false)


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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadPlanesAlimenticios()
        startNotificationPolling()

        // Solo consulta las notificaciones sin reproducir sonido en la primera carga
        // después de la creación de la vista
        loadNotificationCount()
    }

    private fun setupRecyclerView() {
        adapter = PlanAlimenticioAdapter(this)
        binding.recyclerviewPatient.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@PlanListFragment.adapter
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
        if (PlanListFragment.notificationCounts.isEmpty()) return false

        // Verificar si algún paciente tiene más notificaciones que antes
        for ((pacienteId, count) in newCounts) {
            val previousCount = PlanListFragment.notificationCounts[pacienteId] ?: 0
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
                    if (hasNewNotifications && !PlanListFragment.isInitialLoad) {
                        playNotificationSound()
                    }

                    // 5. Actualizar los conteos
                    PlanListFragment.notificationCounts.clear()
                    PlanListFragment.notificationCounts.putAll(newCounts)
                    PlanListFragment.lastNotificationCount = totalCount
                    PlanListFragment.isInitialLoad = false

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

    private fun loadPlanesAlimenticios() {
        val pacienteIds = getPacienteIdsFromSharedPref()
        val nutriologoIds = getNutriologoIdsFromSharedPref()

        if (pacienteIds.isEmpty() || nutriologoIds.isEmpty()) {
            Toast.makeText(requireContext(), "No se encontraron pacientes o nutriólogos", Toast.LENGTH_SHORT).show()
            return
        }

        scope.launch {
            try {
                val response = RetrofitClient.apiService.getPlanesAlimenticios(pacienteIds, nutriologoIds)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val planes = response.body()?.data?.map {
                            PlanAlimenticio(
                                it.Consulta_ID,
                                it.foto_paciente,
                                it.nombre_paciente,
                                it.apellidos,
                                it.nombre_nutriologo,
                                it.enfermedad,
                                it.plan_nutricional_path,
                                formatDate(it.fecha_consulta)
                            )
                        } ?: emptyList()
                        // Ordena por fecha descendente y limita a 20
                        val sortedPlanes = planes.sortedByDescending {
                            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(formatDate(it.fecha_consulta) ?: "")
                        }
                        adapter.submitLimitedList(sortedPlanes)
                    } else {
                        Toast.makeText(requireContext(), "Error al cargar planes alimenticios", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Error de conexión", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            date?.let { outputFormat.format(it) } ?: dateString
        } catch (e: Exception) {
            Log.e("PlanListFragment", "Error al formatear fecha: $dateString", e)
            dateString
        }
    }

    private fun isUrlAccessible(url: String): Boolean {
        return !url.contains("127.0.0.1") && !url.contains("localhost")
    }

    override fun planAlimenticioClicked(planAlimenticio: PlanAlimenticio) {
        try {
            planAlimenticio.plan_nutricional_path?.let { path ->
                // Limpiar el string JSON para obtener las URLs reales
                val cleanedPath = path
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .replace("\\", "")

                val fileUrls = cleanedPath.split(",").map { it.trim() }

                fileUrls.forEach { url ->
                    if (url.isNotBlank()) {
                        if (!isUrlAccessible(url)) {
                            // Reemplazar localhost por la IP del servidor
                            val correctedUrl = url.replace("127.0.0.1", "nutrifitplanner.site")
                                .replace("http://", "https://")
                            downloadFile(correctedUrl)
                        } else {
                            downloadFile(url)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error al descargar: ${e.message}", Toast.LENGTH_SHORT).show()
            Log.e("PlanListFragment", "Error en descarga", e)
        }
    }

    private fun downloadFile(fileUrl: String) {
        try {
            if (fileUrl.isBlank()) {
                Toast.makeText(requireContext(), "URL de descarga inválida", Toast.LENGTH_SHORT).show()
                return
            }

            // Verificar si la URL es válida
            val uri = Uri.parse(fileUrl)
            if (uri.host.isNullOrEmpty()) {
                Toast.makeText(requireContext(), "URL no válida: $fileUrl", Toast.LENGTH_LONG).show()
                return
            }

            val fileName = fileUrl.substringAfterLast("/")
            val destinationPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).toString() + "/" + fileName

            val request = DownloadManager.Request(uri)
                .setTitle("Plan Alimenticio Descargado: $fileName")
                .setDescription("Descargando plan alimenticio")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName)
                .setAllowedOverMetered(true)
                .setAllowedOverRoaming(true)

            val downloadManager = requireContext().getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            // Opcional: Registrar el ID de descarga para verificar el estado después
            Log.d("Download", "Iniciada descarga ID: $downloadId, URL: $fileUrl")
            Toast.makeText(requireContext(), "Descarga iniciada: $fileName", Toast.LENGTH_SHORT).show()

        } catch (e: Exception) {
            Log.e("DownloadError", "Error al descargar: ${e.message}", e)
            Toast.makeText(requireContext(), "Error al descargar: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }




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

        return ids.distinct()
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Planes Alimenticios"
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