package com.ascrib.nutrifit.ui.form

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentOlvidarContraBinding
import com.ascrib.nutrifit.repository.AuthRepository
import com.ascrib.nutrifit.util.getStatusBarHeight
import kotlinx.coroutines.launch

class OlvidoContra : Fragment() {

    lateinit var binding: FragmentOlvidarContraBinding
    private lateinit var authRepository: AuthRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_olvidar_contra, container, false)
        binding.handler = this
        authRepository = AuthRepository(requireContext())  // Inicializa el repositorio
        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutForm.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = ""
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)

            setHasOptionsMenu(true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            findNavController().navigateUp()
        }
        return super.onOptionsItemSelected(item)
    }

    // Lógica para el envío del formulario de recuperación de contraseña
    fun onSubmitClicked() {
        val email = binding.emailEditText.text.toString()
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingrese su correo electrónico", Toast.LENGTH_SHORT).show()
            return
        }

        // Llamada al repositorio para enviar el correo y obtener el enlace de restablecimiento
        recoverPassword(email)
    }

    private fun recoverPassword(email: String) {
        // Aquí llamarías a tu repositorio o API para hacer la solicitud de recuperación de contraseña
        // Asegúrate de manejarlo en un hilo de fondo (usando CoroutineScope) si es necesario
        lifecycleScope.launch {
            val result = authRepository.recoverPassword(email)  // Asume que tienes un método en AuthRepository
            if (result.isSuccess) {
                // Mostrar mensaje de éxito
                Toast.makeText(requireContext(), "Enlace de restablecimiento enviado", Toast.LENGTH_SHORT).show()
                findNavController().navigate(R.id.action_olvidoContraFragment_a_cambioContraFragment)
            } else {
                // Mostrar mensaje de error
                Toast.makeText(requireContext(), "Error al enviar el enlace de restablecimiento", Toast.LENGTH_SHORT).show()
            }
        }
    }
}