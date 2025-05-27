package com.ascrib.nutrifit.ui.dashboard

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.ActivityDashboardBinding
import com.ascrib.nutrifit.util.Statusbar

class DashboardActivity : AppCompatActivity(), NavController.OnDestinationChangedListener{

    lateinit var binding: ActivityDashboardBinding
    private lateinit var navController: NavController


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_dashboard)

        Statusbar.setStatusbarTheme(this, window, 0, binding.root)

        makeDashboard()
    }

    private fun makeDashboard(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.dashboard_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val  popupMenu = PopupMenu(this, binding.root)
        popupMenu.inflate(R.menu.menu_dashboard)
        binding.navigationDashboard.setupWithNavController(popupMenu.menu, navController)

        navController.addOnDestinationChangedListener(this)

    }

    override fun onSupportNavigateUp(): Boolean {
        return super.onSupportNavigateUp()
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when(destination.id){
            R.id.homeFragment,
            R.id.scheduleFragment,
            R.id.profileFragment -> {
                binding.navigationDashboard.visibility = View.VISIBLE
            }
            R.id.chatFragment -> {
                // Inmediatamente regresar y mostrar diálogo
                controller.popBackStack()

                AlertDialog.Builder(this)
                    .setTitle("Sección en Mantenimiento")
                    .setMessage("La sección de chat se encuentra temporalmente en mantenimiento. Estamos trabajando para mejorar tu experiencia.")
                    .setIcon(android.R.drawable.ic_dialog_info)
                    .setPositiveButton("Entendido") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()

                binding.navigationDashboard.visibility = View.VISIBLE
            }
            else -> {
                hideBottomBav()
            }
        }
    }

    private fun hideBottomBav(){
        binding.navigationDashboard.visibility = View.GONE
    }

    fun statusbarIconDark(boolean: Boolean) {
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars =
            boolean
    }

}