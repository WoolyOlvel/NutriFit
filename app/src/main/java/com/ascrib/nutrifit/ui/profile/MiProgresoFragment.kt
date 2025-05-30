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
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.api.ApiService
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.MiProgresoResponse
import com.ascrib.nutrifit.databinding.FragmentMiprogresoBinding
import com.ascrib.nutrifit.repository.NotificacionRepository
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

class MiProgresoFragment: Fragment() {

    lateinit var binding: FragmentMiprogresoBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var apiService: ApiService
    private var consultaId: Int = 0
    private var pacienteId: Int = 0
    private var nutriologoId: Int = 0
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_miprogreso, container, false)
        sharedPref = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        apiService = RetrofitClient.apiService
        Statusbar.setStatusbarTheme(
            requireContext(),
            requireActivity().window,
            R.color.lightGrey,
            binding.root
        )
        mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil)
        // Obtener argumentos
        arguments?.let { bundle ->
            consultaId = bundle.getInt("consultaId", 0)
            pacienteId = bundle.getInt("pacienteId", 0)
            nutriologoId = bundle.getInt("nutriologoId", 0)
        }

        toolbarConfig()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadGcMmData()
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
        if (MiProgresoFragment.notificationCounts.isEmpty()) return false

        // Verificar si algún paciente tiene más notificaciones que antes
        for ((pacienteId, count) in newCounts) {
            val previousCount = MiProgresoFragment.notificationCounts[pacienteId] ?: 0
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
                    if (hasNewNotifications && !MiProgresoFragment.isInitialLoad) {
                        playNotificationSound()
                    }

                    // 5. Actualizar los conteos
                    MiProgresoFragment.notificationCounts.clear()
                    MiProgresoFragment.notificationCounts.putAll(newCounts)
                    MiProgresoFragment.lastNotificationCount = totalCount
                    MiProgresoFragment.isInitialLoad = false

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


    private fun loadGcMmData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getGcMmPorConsulta(
                    pacienteIds = listOf(pacienteId),
                    nutriologoIds = listOf(nutriologoId),
                    consultaIds = listOf(consultaId)
                )

                if (response.isSuccessful && response.body()?.success == true) {
                    val progresoResponse = response.body()!!
                    updateChart(progresoResponse)
                } else {
                    showMessage("Error al obtener datos de progreso")
                }
            } catch (e: Exception) {
                showMessage("Error: ${e.message}")
                Log.d("Error", "Al Cargar: ${e.message}")
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    private fun updateChart(progresoData: MiProgresoResponse) {
        // Encontrar la consulta específica
        val consulta = progresoData.data.flatMap { it.consultas }
            .firstOrNull { it.consulta_id == consultaId }

        consulta?.let {
            val gcValue = it.gc.toFloatOrNull() ?: 0f
            val mmValue = it.mm.toFloatOrNull() ?: 0f

            // Calcular porcentajes
            val total = gcValue + mmValue
            val gcPercent = (gcValue / total) * 100
            val mmPercent = (mmValue / total) * 100

            makePieChart(gcPercent, mmPercent)
            updateTextViews(gcValue, mmValue, gcPercent, mmPercent)
        }
    }

    private fun makePieChart(gcPercent: Float, mmPercent: Float) {
        setupPieChart()

        val entries = ArrayList<PieEntry>().apply {
            add(PieEntry(gcPercent, "Grasa \nCorporal"))
            add(PieEntry(mmPercent, "Masa \nMuscular"))
        }

        val colors = ArrayList<Int>().apply {
            add(ContextCompat.getColor(requireContext(), R.color.colorPrimary))
            add(ContextCompat.getColor(requireContext(), R.color.colorSecondary))
        }

        val dataSet = PieDataSet(entries, "").apply {
            this.colors = colors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
        }

        val data = PieData(dataSet).apply {
            setValueTextSize(10f)
            setValueTypeface(Typeface.SERIF)
            setValueTextColor(Color.WHITE)
        }

        binding.chartMyProgress.apply {
            this.data = data
            invalidate()
            animateY(1500, Easing.EaseInOutQuart)
        }
    }

    private fun updateTextViews(gcValue: Float, mmValue: Float, gcPercent: Float, mmPercent: Float) {
        // Actualizar los TextViews con los valores y porcentajes
        binding.apply {
            textGcValue.text = "%.1f%%".format(gcPercent)
            textMmValue.text = "%.1f%%".format(mmPercent)
            textGcDetail.text = "Grasa Corporal:\n %.1f kg".format(gcValue)
            textMmDetail.text = "Masa Muscular:\n %.1f kg".format(mmValue)
        }
    }

    private fun toolbarConfig(){
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10),0,0)
        }
        binding.toolbar.toolbar.title = "Mi Progreso"
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

    fun makePieChart() {
        setupPieChart()
        loadPieChartData()
    }
    private fun setupPieChart() {
        binding.chartMyProgress.isDrawHoleEnabled = true
        binding.chartMyProgress.setUsePercentValues(false)
        binding.chartMyProgress.setEntryLabelTextSize(12f)
        binding.chartMyProgress.setEntryLabelColor(Color.WHITE)
        binding.chartMyProgress.description.isEnabled = false
        val l : Legend = binding.chartMyProgress.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.isEnabled = false
    }
    private fun loadPieChartData() {
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(201f, "Peso"))
        entries.add(PieEntry(300f, "No Datos"))
        entries.add(PieEntry(320f, "No Datos"))
        entries.add(PieEntry(420f, "No Datos"))
        val colors: ArrayList<Int> = ArrayList()

        colors.add(ContextCompat.getColor(requireContext(),R.color.colorSecondary))
        colors.add(ContextCompat.getColor(requireContext(),R.color.colorPrimary))
        colors.add(ContextCompat.getColor(requireContext(),R.color.colorVariant))
        colors.add(ContextCompat.getColor(requireContext(),R.color.colorVariant2))

        val dataSet = PieDataSet(entries, "")
        dataSet.colors = colors
        val data = PieData(dataSet)
        data.setDrawValues(true)
        data.setValueTextSize(10f)
        data.setValueTypeface(Typeface.SERIF)
        data.setValueTextColor(Color.WHITE)
        binding.chartMyProgress.data = data
        binding.chartMyProgress.invalidate()
        binding.chartMyProgress.animateY(3000, Easing.EaseInOutQuart)
    }
}