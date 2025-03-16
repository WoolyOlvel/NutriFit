package com.ascrib.nutrifit.ui.profile
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
import com.ascrib.nutrifit.databinding.FragmentPerfilSaludBinding
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.utils.MPPointF

class PerfilSaludFragment : Fragment() {

    lateinit var binding: FragmentPerfilSaludBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_perfil_salud, container, false)

        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makePieChart()
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

    fun makePieChart() {
        // Configurar el primer gráfico (proteína)
        setupLineChart(binding.chartLinealSalud, "Proteína", 13f, 17f, "proteina")
        loadLineChartData(
            binding.chartLinealSalud,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Proteína",
            R.color.teal,
            R.color.teal_light
        )

        // Configurar el segundo gráfico (edad corporal)
        setupLineChart(binding.chartLinealSalud2, "Edad Corporal", 14f, 18f, "edad_corporal")
        loadLineChartData(
            binding.chartLinealSalud2,
            getEdadCorporalData(),
            getFormattedDates(),
            "Edad Corporal",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud3, "Masa Esqueletico", 13f, 17f,"masa_esqueletico")
        loadLineChartData(
            binding.chartLinealSalud3,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Masa Esqueletico (Kg)",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud4, "Grasa Vísceral", 12f, 17f,"grasa_visceral")
        loadLineChartData(
            binding.chartLinealSalud4,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Grasa Vísceral",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud5, "Masa Muscular", 11f, 17f,"masa_muscular")
        loadLineChartData(
            binding.chartLinealSalud5,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Masa Muscular",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud6, "Perdida De Grasa", 13f, 17f,"perdida_grasa")
        loadLineChartData(
            binding.chartLinealSalud6,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Perdida De Grasa",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud7, "Grasa Subcutanea", 12f, 17f,"grasa_subcutanea")
        loadLineChartData(
            binding.chartLinealSalud7,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Grasa Subcutanea",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud8, "Músculo Esquelético", 12f, 17f,"musculo_esqueletico")
        loadLineChartData(
            binding.chartLinealSalud8,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Músculo Esquelético",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud9, "BMR", 14f, 17f,"bmr")
        loadLineChartData(
            binding.chartLinealSalud9,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "BMR",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud10, "Grasa Corporal", 12f, 17f,"grasa_corporal")
        loadLineChartData(
            binding.chartLinealSalud10,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Grasa Corporal",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud11, "Agua Corporal", 12f, 17f,"agua")
        loadLineChartData(
            binding.chartLinealSalud11,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Agua Corporal",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud12, "Peso", 12f, 17f,"peso")
        loadLineChartData(
            binding.chartLinealSalud12,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "Peso",
            R.color.teal,
            R.color.teal_light
        )

        setupLineChart(binding.chartLinealSalud13, "IMC", 12f, 17f,"imc")
        loadLineChartData(
            binding.chartLinealSalud13,
            listOf(14.7f, 14.9f, 14.95f, 15.3f, 15.2f),
            getFormattedDates(),
            "IMC",
            R.color.teal,
            R.color.teal_light
        )

    }

    private fun setupLineChart(lineChart: com.github.mikephil.charting.charts.LineChart, title: String, minY: Float, maxY: Float, chartType: String) {
        // Disable description and legend
        lineChart.description.isEnabled = false
        lineChart.legend.isEnabled = false

        // Remove chart border
        lineChart.setDrawBorders(false)

        // Set background color
        lineChart.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.white))

        // Configure X-axis
        val xAxis = lineChart.xAxis
        xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(true)
        xAxis.granularity = 1f
        xAxis.valueFormatter = IndexAxisValueFormatter(getFormattedDates())
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black)
        xAxis.textSize = 10f  // Adjust text size as needed

        // Configure Y-axis
        val yAxisLeft = lineChart.axisLeft
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.gridColor = ContextCompat.getColor(requireContext(), R.color.light_gray)
        yAxisLeft.axisMinimum = minY // Different min values for different charts
        yAxisLeft.axisMaximum = maxY // Different max values for different charts
        yAxisLeft.granularity = 2f
        yAxisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.black)
        yAxisLeft.textSize = 10f  // Adjust text size as needed

        // Disable right Y-axis
        lineChart.axisRight.isEnabled = false

        // Enable touch gestures and highlighting
        lineChart.setTouchEnabled(true)
        lineChart.isDragEnabled = true  // Enable horizontal scrolling
        lineChart.isScaleXEnabled = true  // Enable X-axis scaling
        lineChart.isScaleYEnabled = true  // Enable Y-axis scaling
        lineChart.setPinchZoom(true)  // Enable pinch zoom

        // Set max visible entries (for horizontal scrolling)
        lineChart.setVisibleXRangeMaximum(4f)  // Show 4 entries at a time

        // Setup marker view for tooltip
        val markerView = CustomMarkerView(requireContext(), R.layout.marker_view_layout, chartType)
        lineChart.marker = markerView

        // Enable highlighting
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

        // This method is called when a value is selected/highlighted
        override fun refreshContent(e: Entry?, highlight: Highlight?) {
            if (e != null) {
                // Formatear el valor según el tipo de gráfico
                when (chartType) {
                    "edad_corporal" -> {
                        // Para edad corporal, mostrar como entero
                        tvContent.text = String.format("%d años", e.y.toInt())
                    }
                    "proteina" -> {
                        // Para proteína, mantener un decimal
                        tvContent.text = String.format("%.1f%%", e.y)
                    }"masa_esqueletico" ->{
                        tvContent.text = String.format("%.1f Kg", e.y.toDouble())
                    }"grasa_visceral"->{
                        tvContent.text = String.format("%.1f ", e.y.toDouble())
                    }"masa_muscular"->{
                        tvContent.text = String.format("%.1f Kg", e.y.toDouble())
                    }"perdida_grasa"->{
                        tvContent.text = String.format("%.1f Kg", e.y.toDouble())
                    }"grasa_subcutanea"->{
                        tvContent.text = String.format("%.1f%%", e.y)
                    }"musculo_esqueletico"->{
                        tvContent.text = String.format("%.1f%%", e.y)
                    }"bmr"->{
                        tvContent.text = String.format("%.1f ", e.y.toDouble())
                    }"grasa_corporal"->{
                        tvContent.text = String.format("%.1f%%", e.y)
                    }"agua"->{
                        tvContent.text = String.format("%.1f%%", e.y)
                    }"peso"->{
                        tvContent.text = String.format("%.1f Kg", e.y.toDouble())
                    }"imc"->{
                        tvContent.text = String.format("%.1f ", e.y.toDouble())
                    }else -> {
                        // Formato predeterminado
                        tvContent.text = String.format("%.1f", e.y)
                    }
                }
            }
            super.refreshContent(e, highlight)
        }

        // Offset to ensure the tooltip is drawn at the correct position
        override fun getOffset(): MPPointF {
            return MPPointF(-(width / 2f), -height.toFloat() - 10)
        }
    }

    private fun getEdadCorporalData(): List<Float> {
        return listOf(15.5f, 15.7f, 15.3f, 16.1f, 16.2f) // Datos de ejemplo para el segundo gráfico
    }


}