package com.ascrib.nutrifit.ui.form

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.os.CountDownTimer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentWaitingBinding
import java.util.*

class WaitingFragment : Fragment() {

    private lateinit var binding: FragmentWaitingBinding
    private lateinit var countdownTimer: CountDownTimer
    private var targetDateTime: Long = 0L
    private var startDateTime: Long = 0L
    // Fecha y hora objetivo para el lanzamiento
    private val targetDate = Calendar.getInstance().apply {
        set(2025, Calendar.SEPTEMBER, 25, 10, 0, 0)
        set(Calendar.MILLISECOND, 0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_waiting, container, false)
        binding.handler = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Fecha inicial: 17 de septiembre de 2025 a las 03:13:00 AM
        val startDate = Calendar.getInstance().apply {
            set(Calendar.YEAR, 2025)
            set(Calendar.MONTH, Calendar.SEPTEMBER) // ¡Ojo! Enero = 0, Septiembre = 8
            set(Calendar.DAY_OF_MONTH, 17)
            set(Calendar.HOUR_OF_DAY, 3)  // Hora en formato 24h
            set(Calendar.MINUTE, 13)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        startDateTime = startDate.timeInMillis  // 👈 ahora sí actualiza el campo de la clase
        targetDateTime = targetDate.timeInMillis

        binding.progressBar.visibility = View.VISIBLE
        binding.tvProgress.visibility = View.VISIBLE

        setupAnimations()
        startCountdown()
        checkIfTimeToShowLogin()
    }


    private fun setupAnimations() {
        // Animación de pulso para las ondas
        animatePulse(binding.pulseCircle1, 2000, 0.3f)
        animatePulse(binding.pulseCircle2, 2500, 0.2f)

        // Animación de partículas flotantes
        animateParticles()

        // Animación de elevación del logo card
        val logoElevation = ObjectAnimator.ofFloat(binding.logoCard, "translationY", 0f, -10f, 0f).apply {
            duration = 3000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }
        logoElevation.start()


    }

    private fun animatePulse(view: View, duration: Long, maxAlpha: Float) {
        val scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.4f).apply {
            this.duration = duration
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.4f).apply {
            this.duration = duration
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val alpha = ObjectAnimator.ofFloat(view, "alpha", 0f, maxAlpha, 0f).apply {
            this.duration = duration
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        scaleX.start()
        scaleY.start()
        alpha.start()
    }

    private fun animateParticles() {
        // Animación para partícula 1
        val particle1Y = ObjectAnimator.ofFloat(binding.particle1, "translationY", 0f, -100f, 0f).apply {
            duration = 4000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val particle1Alpha = ObjectAnimator.ofFloat(binding.particle1, "alpha", 0.6f, 0.2f, 0.6f).apply {
            duration = 4000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Animación para partícula 2
        val particle2Y = ObjectAnimator.ofFloat(binding.particle2, "translationY", 0f, 80f, 0f).apply {
            duration = 3500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val particle2X = ObjectAnimator.ofFloat(binding.particle2, "translationX", 0f, -30f, 0f).apply {
            duration = 3500
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Animación para partícula 3
        val particle3Scale = ObjectAnimator.ofFloat(binding.particle3, "scaleX", 1f, 1.5f, 1f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        val particle3ScaleY = ObjectAnimator.ofFloat(binding.particle3, "scaleY", 1f, 1.5f, 1f).apply {
            duration = 2000
            repeatCount = ObjectAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
        }

        // Iniciar todas las animaciones
        particle1Y.start()
        particle1Alpha.start()
        particle2Y.start()
        particle2X.start()
        particle3Scale.start()
        particle3ScaleY.start()
    }

    private fun animateProgress(targetProgress: Int) {
        val currentProgress = binding.progressBar.progress

        val animator = ObjectAnimator.ofInt(binding.progressBar, "progress", currentProgress, targetProgress).apply {
            duration = 500 // medio segundo para que sea suave
            interpolator = LinearInterpolator()
        }
        animator.start()

        binding.tvProgress.text = "$targetProgress% completado"
    }

    private fun startCountdown() {
        val currentTime = System.currentTimeMillis()
        val timeDifference = targetDateTime - currentTime

        if (timeDifference <= 0) {
            navigateToLogin()
            return
        }

        countdownTimer = object : CountDownTimer(timeDifference, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateCountdown(millisUntilFinished)
            }

            override fun onFinish() {
                navigateToLogin()
            }
        }
        countdownTimer.start()
    }

    private fun updateCountdown(millisUntilFinished: Long) {
        val days = millisUntilFinished / (1000 * 60 * 60 * 24)
        val hours = (millisUntilFinished % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60)
        val minutes = (millisUntilFinished % (1000 * 60 * 60)) / (1000 * 60)
        val seconds = (millisUntilFinished % (1000 * 60)) / 1000

        binding.tvDays.text = String.format("%02d", days)
        binding.tvHours.text = String.format("%02d", hours)
        binding.tvMinutes.text = String.format("%02d", minutes)
        binding.tvSeconds.text = String.format("%02d", seconds)


        // 👉 Cálculo del progreso real
        val currentTime = System.currentTimeMillis()
        val totalDuration = targetDateTime - startDateTime
        val elapsed = currentTime - startDateTime
        val percent = ((elapsed.toDouble() / totalDuration.toDouble()) * 100).toInt().coerceIn(0, 100)
        animateProgress(percent)
        binding.progressBar.progress = percent
        binding.tvProgress.text = "$percent% completado"

        // Animación de cambio de números
        animateNumberChange(binding.tvSeconds)
    }

    private fun animateNumberChange(textView: View) {
        // Animación más suave que mantiene el tamaño
        val scaleX = ObjectAnimator.ofFloat(textView, "scaleX", 1f, 1.1f, 1f).apply {
            duration = 500
        }
        val scaleY = ObjectAnimator.ofFloat(textView, "scaleY", 1f, 1.1f, 1f).apply {
            duration = 500
        }

        scaleX.start()
        scaleY.start()
    }

    private fun checkIfTimeToShowLogin() {
        val currentTime = System.currentTimeMillis()
        if (currentTime >= targetDateTime) {
            navigateToLogin()
        }
    }

    private fun navigateToLogin() {
        try {
            findNavController().navigate(R.id.action_waitingFragment_to_loginFragment)
        } catch (e: Exception) {
            // Manejar error de navegación
        }
    }

    fun onSkipWaiting() {
        navigateToLogin()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::countdownTimer.isInitialized) {
            countdownTimer.cancel()
        }
    }
}