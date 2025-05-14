package com.ascrib.nutrifit.ui.dashboard

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentHomeBinding
import com.ascrib.nutrifit.ui.form.adapter.BienvenidaAdapter
import com.ascrib.nutrifit.ui.form.adapter.SliderAdapter
import com.ascrib.nutrifit.util.Statusbar
import com.ascrib.nutrifit.util.getStatusBarHeight
import android.os.Looper
import androidx.viewpager2.widget.ViewPager2

class HomeFragment : Fragment() {

    lateinit var binding: FragmentHomeBinding

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

        toolbarConfig()
        return  binding.root
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
        val userName = sharedPref.getString("user_name", "")

        // Actualizar el TextView
        binding.mensageWelcome.text = "Hola $userName ¡Bienvenido/a \nEstamos encantados de tenerte aquí!"


        binding.progressView.setProgress(55, true)

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
    }

    // Método para iniciar el desplazamiento automático
    private fun startAutoSlide() {
        sliderHandler.postDelayed(sliderRunnable, 3000) // 3 segundos
    }

    override fun onPause() {
        super.onPause()
        sliderHandler.removeCallbacks(sliderRunnable)
    }

    // Reinicia el desplazamiento automático cuando el fragmento vuelve a ser visible
    override fun onResume() {
        super.onResume()
        startAutoSlide()
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
                menuInflater.inflate(R.menu.header_menu,menu)
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

    fun onMyProgessClicked(){
        findNavController().navigate(R.id.action_homeFragment_a_myProgressFragment)
    }

    fun onPlanAlimentClicked(){
        findNavController().navigate(R.id.global_planListFragment)
    }

    fun onNutriDefOnClicked(){
        findNavController().navigate(R.id.global_desafioNutriFragment)
    }

    fun onReportSaludClicked(){
        findNavController().navigate(R.id.action_homeFragment_a_myPersonSaludFragment)
    }

    fun onHistorialNutriClicked(){
        findNavController().navigate(R.id.action_homeFragment_a_historyNutriListFragment)
    }



}