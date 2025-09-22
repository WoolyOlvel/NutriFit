package com.ascrib.nutrifit.ui.form

import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentFinishedBinding

class FinishedFragment : Fragment() {

    private lateinit var binding: FragmentFinishedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_finished, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configurar animaciones similares al waiting fragment
        setupAnimations()
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
}