package com.ascrib.nutrifit.ui.profile

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.setMargins
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentMyprogressBinding
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class ProgressFragment : Fragment() {

    lateinit var binding: FragmentMyprogressBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_myprogress, container, false)

        toolbarConfig()
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        makePieChart()

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