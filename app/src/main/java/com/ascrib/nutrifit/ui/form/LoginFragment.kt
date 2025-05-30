package com.ascrib.nutrifit.ui.form

import android.content.Intent
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
import com.ascrib.nutrifit.databinding.FragmentLoginBinding
import com.ascrib.nutrifit.ui.dashboard.DashboardActivity
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.ascrib.nutrifit.api.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CoroutineScope
import com.ascrib.nutrifit.api.models.LoginRequest


class LoginFragment : Fragment() {

    lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(layoutInflater, R.layout.fragment_login, container, false)
        binding.handler = this
        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutForm.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        // Intentar hacer un auto-login si hay un remember_token
        checkAutoLogin()
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
        // Obtener los valores de los campos de correo y contraseña

        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        val rememberMeChecked = binding.rememberMeCheckbox.isChecked

        // Verificar que los campos no estén vacíos
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(context, "Por favor ingrese su correo y contraseña.", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear el objeto LoginRequest con los datos ingresados
        val loginRequest = LoginRequest(email, password, rememberMeChecked, is_mobile = true)


        // Llamada Retrofit para realizar el login
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Pasar el objeto LoginRequest a la llamada Retrofit
                val response = RetrofitClient.apiService.login(loginRequest)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val data = response.body()

                        data?.let {
                            // Guardar datos del usuario
                            val sharedPref = activity?.getSharedPreferences(
                                "user_data",
                                AppCompatActivity.MODE_PRIVATE
                            )
                            sharedPref?.edit()?.apply {
                                putString("user_name", it.user?.nombre)
                                putString("usuario", it.user?.usuario)
                                putString("user_lastname", it.user?.apellidos)
                                putString("user_email", it.user?.email)
                                putInt("user_rol_id", it.user?.rol_id ?: 2)
                                it.user?.id?.let { it1 -> putInt("user_id", it1) }
                                apply()
                            }
                        }



                            // Guardar remember_token si el login es exitoso
                        data?.remember_token?.let { token ->
                            val sharedPref = activity?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                            val editor = sharedPref?.edit()

                            // Si "Recordarme" está activado, guardar el token
                            editor?.putString("remember_token", token)

                            // Si la opción "Recordarme" está seleccionada, también guardamos el estado
                            editor?.putBoolean("remember_me", rememberMeChecked)

                            editor?.apply()
                        }

                        // Redirigir a la actividad del Dashboard
                        startActivity(Intent(context, DashboardActivity::class.java))
                        activity?.finishAffinity()
                    } else {
                        // Mostrar mensaje si la respuesta no es exitosa
                        Toast.makeText(context, "Error al iniciar sesión. Verifique sus credenciales.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    // Mostrar mensaje si ocurre algún error de conexión
                    Toast.makeText(context, "Error de conexión. Intente nuevamente.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    fun onRegisterClicked() {
        findNavController().navigate(R.id.action_loginFragment_a_registerFragment)
    }

    fun onOlvidarContraClicked() {
        findNavController().navigate(R.id.action_loginFragment_a_olvidoContraFragment)
    }

    private fun checkAutoLogin() {
        val sharedPref = activity?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val rememberToken = sharedPref?.getString("remember_token", null)

        if (rememberToken != null) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.apiService.autoLogin(rememberToken)
                    withContext(Dispatchers.Main) {
                        if (response.isSuccessful) {
                            val data = response.body()

                            // Verificar si la respuesta tiene los datos del usuario
                            data?.user?.let { user ->
                                // Guardar datos del usuario (igual que en login normal)
                                sharedPref?.edit()?.apply {
                                    putString("user_name", user.nombre)
                                    putString("user_lastname", user.apellidos)
                                    putString("usuario", user.usuario)
                                    putString("user_email", user.email)
                                    putInt("user_rol_id", user.rol_id ?: 2)
                                    putInt("user_id", user.id)
                                    apply()
                                }

                                // Mostrar mensaje de éxito
                                Toast.makeText(context, "Bienvenido de nuevo, ${user.nombre}.", Toast.LENGTH_SHORT).show()

                                // Redirigir
                                startActivity(Intent(context, DashboardActivity::class.java))
                                activity?.finishAffinity()
                            }
                        } else {
                            // Limpiar token inválido
                            sharedPref?.edit()?.remove("remember_token")?.apply()
                            Toast.makeText(context, "Sesión expirada, por favor inicie sesión nuevamente.", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Error de conexión. Intente nuevamente.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }
}
