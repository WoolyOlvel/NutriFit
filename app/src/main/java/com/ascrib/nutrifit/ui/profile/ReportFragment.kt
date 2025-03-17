package com.ascrib.nutrifit.ui.profile

import android.graphics.Color
import android.graphics.Typeface
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
import com.cninfotech.swasthyedoctor.R
import com.cninfotech.swasthyedoctor.databinding.FragmentFinanceBinding
import com.cninfotech.swasthyedoctor.util.getStatusBarHeight

import com.github.mikephil.charting.data.PieData

import com.github.mikephil.charting.data.PieDataSet

import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.animation.Easing




class ReportFragment : Fragment() {
    lateinit var binding: FragmentFinanceBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_finance, container, false)

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

        binding.toolbar.toolbar.title = "Report"
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

    fun makePieChart() {
        setupPieChart()
        loadPieChartData()
    }

    private fun setupPieChart() {
        binding.chartReport.isDrawHoleEnabled = true
        binding.chartReport.setUsePercentValues(false)
        binding.chartReport.setEntryLabelTextSize(12f)
        binding.chartReport.setEntryLabelColor(Color.WHITE)
        binding.chartReport.description.isEnabled = false
        val l: Legend = binding.chartReport.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.TOP
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.RIGHT
        l.orientation = Legend.LegendOrientation.VERTICAL
        l.setDrawInside(false)
        l.isEnabled = false
    }

    private fun loadPieChartData() {
        val entries: ArrayList<PieEntry> = ArrayList()
        entries.add(PieEntry(1400f, "Audio Call"))
        entries.add(PieEntry(2000f, "Video Call"))
        entries.add(PieEntry(800f, "Live Chat"))
        entries.add(PieEntry(600f, "Message"))
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
        binding.chartReport.data = data
        binding.chartReport.invalidate()
        binding.chartReport.animateY(3000, Easing.EaseInOutQuart)
    }
}