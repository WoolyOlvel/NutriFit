package com.ascrib.nutrifit.ui.profile
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
import android.widget.TextView
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
import com.ascrib.nutrifit.api.models.ConsultaAntropometrica
import com.ascrib.nutrifit.databinding.FragmentMiperfilsaludBinding
import com.ascrib.nutrifit.databinding.FragmentPerfilSaludBinding
import com.ascrib.nutrifit.repository.NotificacionRepository
import com.ascrib.nutrifit.util.Statusbar
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class MiPerfilSaludFragment : Fragment() {

    lateinit var binding: FragmentMiperfilsaludBinding
    private lateinit var sharedPref: SharedPreferences
    private lateinit var apiService: ApiService
    private var pacienteId: Int = 0
    private var nutriologoId: Int = 0

    // Listas para almacenar los datos de las consultas
    private var consultasFiltradas: List<ConsultaAntropometrica> = emptyList()
    private var fechasFormateadas: List<String> = emptyList()
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
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_miperfilsalud, container, false)
        sharedPref = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        apiService = RetrofitClient.apiService
        Statusbar.setStatusbarTheme(
            requireContext(),
            requireActivity().window,
            R.color.lightGrey,
            binding.root
        )
        mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil)

        // Obtener argumentos de forma más robusta
        arguments?.let {
            pacienteId = it.getInt("pacienteId", sharedPref.getInt("Paciente_ID", 0))
            nutriologoId = it.getInt("nutriologoId", 0)

            // Debug: Verificar valores
            Log.d("MiPerfilSalud", "pacienteId: $pacienteId, nutriologoId: $nutriologoId")
        } ?: run {
            // Si no hay argumentos, intentar obtener de SharedPreferences
            pacienteId = sharedPref.getInt("Paciente_ID", 0)
            nutriologoId = sharedPref.getInt("NUTRIOLOGO_ID", 0)
        }

        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Configurar todas las gráficas primero
        setupAllCharts()

        // Obtener datos del servidor
        fetchConsultasData()
        startNotificationPolling()
        loadNotificationCount()
    }

    private fun fetchConsultasData() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = apiService.getConsultaPorPaciente5(
                    pacienteIds = listOf(pacienteId),
                    nutriologoIds = listOf(nutriologoId)
                )

                if (response.isSuccessful) {
                    val consultaGraficas = response.body()
                    consultaGraficas?.let { graficas ->
                        // Filtrar las consultas del nutriólogo específico
                        val nutriologoData = graficas.data.find { data ->
                            data.nutriologo_id  == nutriologoId
                        }

                        nutriologoData?.let { data ->
                            // Verificar que data.consulta no sea nulo
                            val consultas = data.consultas ?: emptyList()

                            // Ordenar consultas por fecha (usando fecha_creacion de cada consulta)
                            consultasFiltradas = consultas.sortedBy { consulta ->
                                SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                    .parse(consulta.fecha_creacion ?: "")?.time ?: 0
                            }

                            // Formatear fechas (usando fecha_creacion de cada consulta)
                            fechasFormateadas = consultasFiltradas.map { consulta ->
                                consulta.fecha_creacion?.let { fecha ->
                                    formatDateToDayMonth(fecha)
                                } ?: "N/A"
                            }

                            // Actualizar UI con los datos
                            withContext(Dispatchers.Main) {
                                updateAllChartsWithData()
                            }
                        } ?: run {
                            // Si no hay datos del nutriólogo, mostrar mensaje o manejar adecuadamente
                            withContext(Dispatchers.Main) {
                                // Mostrar mensaje al usuario
                            }
                        }
                    }
                } else {
                    // Manejar error
                    withContext(Dispatchers.Main) {
                        // Mostrar mensaje de error al usuario
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    // Mostrar mensaje de error al usuario
                    Toast.makeText(context, "Error al cargar datos a las gráficas", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun formatDateToDayMonth(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""

        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString ?: ""
        }
    }

    private fun setupAllCharts() {
        // Configurar todas las gráficas con rangos iniciales
        setupLineChart(binding.chartLinealSalud, "Proteína (%)", 0f, 100f, "proteina")
        setupLineChart(binding.chartLinealSalud2, "Edad Corporal", 0f, 100f, "edad_corporal")
        setupLineChart(binding.chartLinealSalud3, "Masa Esquelética (Kg)", 0f, 100f, "masa_esqueletico")
        setupLineChart(binding.chartLinealSalud4, "Grasa Vísceral", 0f, 100f, "grasa_visceral")
        setupLineChart(binding.chartLinealSalud5, "Masa Muscular (Kg)", 0f, 100f, "masa_muscular")
        setupLineChart(binding.chartLinealSalud6, "Pérdida De Grasa (Kg)", 0f, 100f, "perdida_grasa")
        setupLineChart(binding.chartLinealSalud7, "Grasa Subcutánea (%)", 0f, 100f, "grasa_subcutanea")
        setupLineChart(binding.chartLinealSalud8, "Músculo Esquelético (%)", 0f, 100f, "musculo_esqueletico")
        setupLineChart(binding.chartLinealSalud9, "BMR (kcal)", 0f, 3000f, "bmr")
        setupLineChart(binding.chartLinealSalud10, "Grasa Corporal (%)", 0f, 100f, "grasa_corporal")
        setupLineChart(binding.chartLinealSalud11, "Agua Corporal (%)", 0f, 100f, "agua")
        setupLineChart(binding.chartLinealSalud12, "Peso (Kg)", 0f, 200f, "peso")
        setupLineChart(binding.chartLinealSalud13, "IMC", 0f, 50f, "imc")
        setupLineChart(binding.chartLinealSalud14, "Edad Metabólica", 0f, 100f, "edad_metabolica")

    }

    private fun updateAllChartsWithData() {
        if (consultasFiltradas.isEmpty() || fechasFormateadas.isEmpty()) return

        // Proteína (%)
        updateChartWithData(
            binding.chartLinealSalud,
            consultasFiltradas.mapNotNull { it.proteina?.toFloatOrNull() },
            "Proteína (%)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView
        )

        // Edad Corporal
        updateChartWithData(
            binding.chartLinealSalud2,
            consultasFiltradas.mapNotNull { it.ec?.toFloatOrNull() },
            "Edad Corporal",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView2
        )

        // Masa Esquelética (Kg)
        updateChartWithData(
            binding.chartLinealSalud3,
            consultasFiltradas.mapNotNull { it.me?.toFloatOrNull() },
            "Masa Esquelética (Kg)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView3
        )

        // Grasa Vísceral
        updateChartWithData(
            binding.chartLinealSalud4,
            consultasFiltradas.mapNotNull { it.gv?.toFloatOrNull() },
            "Grasa Vísceral",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView4
        )

        // Masa Muscular (Kg)
        updateChartWithData(
            binding.chartLinealSalud5,
            consultasFiltradas.mapNotNull { it.mm?.toFloatOrNull() },
            "Masa Muscular (Kg)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView5
        )

        // Pérdida De Grasa (Kg)
        updateChartWithData(
            binding.chartLinealSalud6,
            consultasFiltradas.mapNotNull { it.pg?.toFloatOrNull() },
            "Pérdida De Grasa (Kg)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView6
        )

        // Grasa Subcutánea (%)
        updateChartWithData(
            binding.chartLinealSalud7,
            consultasFiltradas.mapNotNull { it.gs?.toFloatOrNull() },
            "Grasa Subcutánea (%)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView7
        )

        // Músculo Esquelético (%)
        updateChartWithData(
            binding.chartLinealSalud8,
            consultasFiltradas.mapNotNull { it.meq?.toFloatOrNull() },
            "Músculo Esquelético (%)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView8
        )

        // BMR (kcal)
        updateChartWithData(
            binding.chartLinealSalud9,
            consultasFiltradas.mapNotNull { it.bmr?.toFloatOrNull() },
            "BMR (kcal)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView9
        )

        // Grasa Corporal (%)
        updateChartWithData(
            binding.chartLinealSalud10,
            consultasFiltradas.mapNotNull { it.gc?.toFloatOrNull() },
            "Grasa Corporal (%)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView10
        )

        // Agua Corporal (%)
        updateChartWithData(
            binding.chartLinealSalud11,
            consultasFiltradas.mapNotNull { it.ac?.toFloatOrNull() },
            "Agua Corporal (%)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView11
        )

        // Peso (Kg)
        updateChartWithData(
            binding.chartLinealSalud12,
            consultasFiltradas.mapNotNull { it.peso?.toFloatOrNull() },
            "Peso (Kg)",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView12
        )

        // IMC
        updateChartWithData(
            binding.chartLinealSalud13,
            consultasFiltradas.mapNotNull { it.imc?.toFloatOrNull() },
            "IMC",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView13
        )

        // Edad Metabólica
        updateChartWithData(
            binding.chartLinealSalud14,
            consultasFiltradas.mapNotNull { it.em?.toFloatOrNull() },
            "Edad Metabólica",
            R.color.teal,
            R.color.teal_light,
            binding.titleTextView14
        )
    }

    private fun updateChartWithData(
        lineChart: com.github.mikephil.charting.charts.LineChart,
        values: List<Float>,
        label: String,
        lineColor: Int,
        fillColor: Int,
        titleTextView: TextView? = null
    ) {
        if (values.isEmpty()){
            titleTextView?.text = "$label: N/A"
            return
        }

        // Calcular min y max para ajustar el rango del gráfico
        val minY = (values.minOrNull() ?: 0f) * 0.9f
        val maxY = (values.maxOrNull() ?: 100f) * 1.1f

        // Configurar eje Y
        lineChart.axisLeft.axisMinimum = minY
        lineChart.axisLeft.axisMaximum = maxY

        // Crear entradas para el gráfico
        val entries = values.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }

        val lastValue = values.lastOrNull()
        titleTextView?.text = "$label: ${formatValueForDisplay(lastValue, label)}"

        // Configurar listener para cambios de selección
        lineChart.setOnChartValueSelectedListener(object : com.github.mikephil.charting.listener.OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                try {
                    e?.let {
                        titleTextView?.text = "$label: ${formatValueForDisplay(e.y, label)}"
                    }
                } catch (ex: Exception) {
                    Log.e("ChartError", "Error formatting selected value", ex)
                }
            }

            override fun onNothingSelected() {
                try {
                    titleTextView?.text = "$label: ${formatValueForDisplay(values.lastOrNull(), label)}"
                } catch (ex: Exception) {
                    Log.e("ChartError", "Error formatting last value", ex)
                }
            }
        })

        // Configurar conjunto de datos
        val dataSet = LineDataSet(entries, label).apply {
            color = ContextCompat.getColor(requireContext(), lineColor)
            lineWidth = 2f
            setDrawCircles(true)
            circleRadius = 4f
            setCircleColor(ContextCompat.getColor(requireContext(), lineColor))
            circleHoleColor = ContextCompat.getColor(requireContext(), android.R.color.white)
            circleHoleRadius = 2f
            setDrawFilled(true)
            fillAlpha = 50
            // Corrección: usar setFillColor en lugar de fillColor
            setFillColor(ContextCompat.getColor(requireContext(), fillColor))
            setDrawValues(false)
            highLightColor = ContextCompat.getColor(requireContext(), R.color.teal_dark)
            highlightLineWidth = 1.5f
            setDrawHighlightIndicators(true)
            enableDashedHighlightLine(10f, 5f, 0f)
        }

        // Configurar eje X con fechas
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(fechasFormateadas)

        // Establecer datos y actualizar gráfico
        lineChart.data = LineData(dataSet)
        lineChart.invalidate()
    }

    private fun formatValueForDisplay(value: Float?, label: String): String {
        return when {
            value == null -> "N/A"
            label.contains("%") || label == "IMC" -> "%.1f%%".format(value) // IMC con % si lo deseas
            label.contains("kcal") -> "%.0f kcal".format(value)
            label.contains("Kg") -> "%.1f Kg".format(value)
            label.contains("Edad Corporal") -> "%.0f años".format(value)
            label.contains("Edad Metabólica") -> "%.0f años".format(value)
            label == "IMC" -> "%.1f".format(value) // IMC sin % (formato estándar)
            else -> "%.1f".format(value)
        }
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Perfil Salud"
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
                    }R.id.action_notification ->{
                        findNavController().navigate(R.id.global_notificationFragment)
                        return true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    private fun setupLineChart(
        lineChart: com.github.mikephil.charting.charts.LineChart,
        title: String,
        minY: Float,
        maxY: Float,
        chartType: String
    ) {
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false
        lineChart.setDrawBorders(false)
        lineChart.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))

        val xAxis = lineChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)
        xAxis.textSize = 10f

        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
        yAxisLeft.axisMinimum = minY
        yAxisLeft.axisMaximum = maxY
        yAxisLeft.granularity = (maxY - minY) / 5
        yAxisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.black)
        yAxisLeft.textSize = 10f

        lineChart.axisRight.isEnabled = false
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true
        lineChart.isScaleXEnabled = true
        lineChart.isScaleYEnabled = true
        lineChart.setPinchZoom(true)
        lineChart.setVisibleXRangeMaximum(4f)

        val markerView = CustomMarkerView(requireContext(), R.layout.marker_view_layout, chartType)
        lineChart.marker = markerView

        lineChart.isHighlightPerDragEnabled = true
        lineChart.isHighlightPerTapEnabled = true
    }

    private fun loadLineChartData(
        lineChart: com.github.mikephil.charting.charts.LineChart,
        values: List<Float>,
        dates: List<String>,
        label: String,
        lineColor: Int,
        fillColor: Int
    ) {
        val entries = values.mapIndexed { index, value -> Entry(index.toFloat(), value) }

        val dataSet = LineDataSet(entries, label)

        // Configure line appearance
        dataSet.color = ContextCompat.getColor(requireContext(), lineColor)
        dataSet.lineWidth = 2f

        // Configure data points
        dataSet.setDrawCircles(true)
        dataSet.circleRadius = 4f
        dataSet.setCircleColor(ContextCompat.getColor(requireContext(), lineColor))
        dataSet.circleHoleColor = ContextCompat.getColor(requireContext(), android.R.color.white)
        dataSet.circleHoleRadius = 2f

        // Configure fill area
        dataSet.setDrawFilled(true)
        dataSet.fillAlpha = 50
        dataSet.fillColor = ContextCompat.getColor(requireContext(), fillColor)

        // Configure value text - dont show it directly on points
        dataSet.setDrawValues(false)

        // Enable highlighting of values
        dataSet.highLightColor = ContextCompat.getColor(requireContext(), R.color.teal_dark)
        dataSet.highlightLineWidth = 1.5f
        dataSet.setDrawHighlightIndicators(true)
        dataSet.enableDashedHighlightLine(10f, 5f, 0f)

        // Create and set data
        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Animate the chart
        lineChart.animateX(1000)

        // Refresh the chart
        lineChart.invalidate()
    }
    // Formato de fechas en el eje X
    private fun getFormattedDates(): List<String> {
        return listOf("14/02", "21/02", "26/02", "03/03", "13/03")
    }

    class CustomMarkerView(context: Context, layoutResource: Int, private val chartType: String = "default") :
        MarkerView(context, layoutResource) {

        private val tvContent: TextView = findViewById(R.id.tvContent)

        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            if (e != null) {
                when (chartType) {
                    "edad_corporal", "edad_metabolica" , -> {
                        tvContent.text = String.format("%d años", e.y.toInt())
                    }
                    "proteina", "grasa_subcutanea", "musculo_esqueletico",
                    "grasa_corporal", "agua" , "imc" -> {
                        tvContent.text = String.format("%.1f%%", e.y)
                    }
                    "masa_esqueletico", "masa_muscular", "perdida_grasa", "peso" -> {
                        tvContent.text = String.format("%.1f Kg", e.y)
                    }
                    "grasa_visceral",  -> {
                        tvContent.text = String.format("%.1f%%", e.y)
                    }
                    "bmr" -> {
                        tvContent.text = String.format("%.1f kcal", e.y)
                    }
                    else -> {
                        tvContent.text = String.format("%.1f", e.y)
                    }
                }
            }
            super.refreshContent(e, highlight)
        }

        override fun getOffset(): MPPointF {
            return MPPointF(-(width / 2f), -height.toFloat() - 10)
        }
    }

    private fun getEdadCorporalData(): List<Float> {
        return listOf(15.5f, 15.7f, 15.3f, 16.1f, 16.2f) // Datos de ejemplo para el segundo gráfico
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
        if (MiPerfilSaludFragment.notificationCounts.isEmpty()) return false

        // Verificar si algún paciente tiene más notificaciones que antes
        for ((pacienteId, count) in newCounts) {
            val previousCount = MiPerfilSaludFragment.notificationCounts[pacienteId] ?: 0
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
                    if (hasNewNotifications && !MiPerfilSaludFragment.isInitialLoad) {
                        playNotificationSound()
                    }

                    // 5. Actualizar los conteos
                    MiPerfilSaludFragment.notificationCounts.clear()
                    MiPerfilSaludFragment.notificationCounts.putAll(newCounts)
                    MiPerfilSaludFragment.lastNotificationCount = totalCount
                    MiPerfilSaludFragment.isInitialLoad = false

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