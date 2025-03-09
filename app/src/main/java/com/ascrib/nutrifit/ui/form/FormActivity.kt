package com.ascrib.nutrifit.ui.form

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowInsetsControllerCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.ActivityFormBinding
import com.ascrib.nutrifit.util.Statusbar

class FormActivity: AppCompatActivity() {

    private lateinit var binding: ActivityFormBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_form)

        Statusbar.setStatusbarTheme(this,window, 0, binding.root)

        makeNavHost()
    }

    private fun makeNavHost(){
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.dashboard_nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
    }

    fun statusBarIconDark(boolean: Boolean){
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars=
            boolean
    }


}