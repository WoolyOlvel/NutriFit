package com.ascrib.nutrifit.ui.profile

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.graphics.Typeface
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.api.ApiService
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.ConsultaData
import com.ascrib.nutrifit.databinding.FragmentMyprogressBinding
import com.ascrib.nutrifit.repository.NotificacionRepository
import com.ascrib.nutrifit.ui.dashboard.adapter.NutriologosAdapter
import com.ascrib.nutrifit.ui.planList.HistoryNutriListFragment
import com.ascrib.nutrifit.util.Statusbar
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProgressFragment : Fragment() {

    private lateinit var binding: FragmentMyprogressBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var apiService: ApiService
    private lateinit var adapter: NutriologosAdapter
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_myprogress, container, false)
        sharedPref = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        apiService = RetrofitClient.apiService
        Statusbar.setStatusbarTheme(
            requireContext(),
            requireActivity().window,
            R.color.lightGrey,
            binding.root
        )
        mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil)
        setupRecyclerView()
        loadConsultas()
        toolbarConfig()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        startNotificationPolling()
        loadNotificationCount()

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
        if (ProgressFragment.notificationCounts.isEmpty()) return false

        // Verificar si algún paciente tiene más notificaciones que antes
        for ((pacienteId, count) in newCounts) {
            val previousCount = ProgressFragment.notificationCounts[pacienteId] ?: 0
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
                    if (hasNewNotifications && !ProgressFragment.isInitialLoad) {
                        playNotificationSound()
                    }

                    // 5. Actualizar los conteos
                    ProgressFragment.notificationCounts.clear()
                    ProgressFragment.notificationCounts.putAll(newCounts)
                    ProgressFragment.lastNotificationCount = totalCount
                    ProgressFragment.isInitialLoad = false

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

    private fun setupRecyclerView() {
        adapter = NutriologosAdapter(emptyList()) { consulta ->
            onConsultaClicked(consulta)
        }

        binding.recyclerViewNutriologos.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewNutriologos.adapter = adapter
    }

    private fun loadConsultas() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val pacienteIds = getPacienteIdsFromSharedPref()
                val nutriologoIds = getNutriologoIdsFromSharedPref()

                if (pacienteIds.isEmpty() || nutriologoIds.isEmpty()) {
                    showMessage("No se encontraron IDs de paciente o nutriólogo")
                    return@launch
                }

                val response = apiService.getConsultasPorPaciente3(pacienteIds, nutriologoIds)

                if (response.isSuccessful && response.body()?.success == true) {
                    val consultas = response.body()?.data ?: emptyList()
                    adapter = NutriologosAdapter(consultas) { consulta ->
                        onConsultaClicked(consulta)
                    }
                    binding.recyclerViewNutriologos.adapter = adapter
                } else {
                    showMessage("Error al obtener consultas: ${response.message()}")
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
            }
        }
    }



    fun onConsultaClicked(consulta: ConsultaData) {
        // Guardar los IDs necesarios en SharedPreferences o pasarlos como argumentos
        sharedPref.edit().apply {
            putInt("CONSULTA_ID", consulta.consulta_id)
            putInt("PACIENTE_ID", consulta.paciente_id)
            putInt("NUTRIOLOGO_ID", consulta.nutriologo_id)
            apply()
        }

        findNavController().navigate(
            R.id.miProgresoFragment,
            bundleOf(
                "consultaId" to consulta.consulta_id,
                "pacienteId" to consulta.paciente_id,
                "nutriologoId" to consulta.nutriologo_id
            )
        )
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

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun toolbarConfig(){
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10),0,0)
        }
        binding.toolbar.toolbar.title = "Mis Seguimientos"
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
                    R.id.action_notification ->{
                        findNavController().navigate(R.id.global_notificationFragment)
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

    }

}