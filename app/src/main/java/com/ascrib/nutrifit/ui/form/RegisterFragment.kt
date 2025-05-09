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
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentRegistrateBinding
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.LoginRequest
import com.ascrib.nutrifit.api.models.RegisterRequest
import com.ascrib.nutrifit.util.getStatusBarHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class RegisterFragment : Fragment() {

    lateinit var binding: FragmentRegistrateBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_registrate, container, false)
        binding.handler = this
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

    fun onLoginClicked() {
        findNavController().navigate(R.id.action_registerFragment_a_loginFragment)
    }

    fun onRegisterClicked() {
        val nombre = binding.nombreEditText.text.toString().trim()
        val apellidos = binding.apellidosEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val usuario = binding.usuarioEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString()

        // Validación de campos
        var valid = true
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            valid = false
            Toast.makeText(context, "Por favor complete todos los campos", Toast.LENGTH_SHORT).show()
        }

        // Validación de contraseña (mínimo 8 caracteres, al menos un número, una letra minúscula y una mayúscula)
        val passwordPattern = "^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$".toRegex()
        if (!passwordPattern.matches(password)) {
            valid = false
            Toast.makeText(context, "La contraseña debe tener al menos 8 caracteres, con mayúsculas, minúsculas y números", Toast.LENGTH_SHORT).show()
        }

        if (!valid) return

        // Crear objeto de datos para registro
        val userData = RegisterRequest(
            nombre = nombre,
            apellidos = apellidos,
            email = email,
            usuario = usuario,
            password = password,
            rol_id = 2  // Puedes ajustar el valor de rol_id según sea necesario
        )

        // Mostrar indicador de carga
        Toast.makeText(context, "Registrando...", Toast.LENGTH_SHORT).show()

        // Enviar la solicitud a la API
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.apiService.register(userData)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        Toast.makeText(context, "¡Registro exitoso!", Toast.LENGTH_SHORT).show()
                        // Redirigir a la pantalla de login
                        findNavController().navigate(R.id.action_registerFragment_a_loginFragment)
                    } else {
                        // Mostrar mensaje de error si no es exitoso
                        val errorMessage = response.errorBody()?.string() ?: "Error desconocido"
                        Toast.makeText(context, "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error al registrar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error de conexión. Intente nuevamente.", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}


