package com.ascrib.nutrifit

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.ascrib.nutrifit.databinding.ActivityMainBinding
import com.ascrib.nutrifit.ui.form.FormActivity
import com.ascrib.nutrifit.util.Statusbar

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        Statusbar.setStatusbarTheme(this, window, 0, binding.root)
    }

    private fun changeActivity() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(applicationContext, FormActivity::class.java))
            finish()
        }, 500)
    }

    override fun onResume() {
        super.onResume()
        changeActivity()
    }

}