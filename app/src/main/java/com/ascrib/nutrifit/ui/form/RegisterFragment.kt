package com.ascrib.nutrifit.ui.form

import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentRegistrateBinding
import com.ascrib.nutrifit.repository.AuthRepository
import com.ascrib.nutrifit.ui.dashboard.DashboardActivity
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.os.Handler
import android.os.Looper

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegistrateBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrar ActivityResultLauncher para Google Sign-In
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_registrate, container, false)
        binding.handler = this
        authRepository = AuthRepository(requireContext())
        toolbarConfig()

        // Configuración de Google Sign-In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireContext(), googleSignInOptions)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutForm.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, requireActivity().getStatusBarHeight().plus(10), 0, 0)
        }

        // Botón Google
        binding.googleButton.setOnClickListener {
            onGoogleSignInClicked(it)
        }

        // Ocultar botón de Facebook si existe en el layout
        try {
            binding.facebookButton.visibility = View.GONE
        } catch (e: Exception) {
            // El botón puede no existir en el layout, ignoramos el error
        }
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, requireActivity().getStatusBarHeight().plus(10), 0, 0)
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
            return true
        }
        return super.onOptionsItemSelected(item)
    }


    fun onRegisterClicked() {
        val nombre = binding.nombreEditText.text.toString().trim()
        val apellidos = binding.apellidosEditText.text.toString().trim()
        val email = binding.emailEditText.text.toString().trim()
        val usuario = binding.usuarioEditText.text.toString().trim()
        val password = binding.passwordEditText.text.toString().trim()

        // Verificación de campos vacíos
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || usuario.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Todos los campos son obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Validación de la contraseña
        val passwordPattern = Regex("^(?=.*\\d)(?=.*[a-z])(?=.*[A-Z]).{8,}$")
        if (!passwordPattern.matches(password)) {
            Toast.makeText(requireContext(), "La contraseña debe tener al menos 8 caracteres, una mayúscula, una minúscula y un número", Toast.LENGTH_LONG).show()
            return
        }

        lifecycleScope.launch {
            try {
                // Mostrar un mensaje de carga
                val loadingToast = Toast.makeText(requireContext(), "Procesando registro...", Toast.LENGTH_SHORT)
                loadingToast.show()

                val result = authRepository.register(nombre, apellidos, email, usuario, password)

                // Ocultar mensaje de carga
                loadingToast.cancel()

                when {
                    result.isSuccess -> {
                        val response = result.getOrNull()

                        if (response == null) {
                            Toast.makeText(requireContext(), "Error: Respuesta vacía del servidor", Toast.LENGTH_LONG).show()
                            return@launch
                        }

                        if (response.success) {
                            // Registro exitoso
                             Toast.makeText(requireContext(), "¡Registro exitoso!", Toast.LENGTH_SHORT).show()

                            // Usar un Handler para esperar antes de hacer la navegación
                            findNavController().navigate(R.id.action_registerFragment_a_loginFragment)
                        } else {
                            // Respuesta con success=false
                            val errorMsg = response.message ?: "Error desconocido en el registro"
                            Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                        }
                    }
                    else -> {
                        // Respuesta fallida (excepción)
                        val exception = result.exceptionOrNull()
                        val errorMessage = exception?.message ?: "Error desconocido en el registro"
                        Toast.makeText(requireContext(), "Error: $errorMessage", Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                // Manejo de excepciones no capturadas
                Toast.makeText(requireContext(), "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    fun onLoginClicked() {
        findNavController().navigate(R.id.action_registerFragment_a_loginFragment)
    }



    fun onGoogleSignInClicked(view: View) {
        val signInIntent = googleSignInClient.signInIntent
        googleSignInLauncher.launch(signInIntent)
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            // Éxito en el inicio de sesión con Google
            val idToken = account?.idToken
            if (idToken != null) {
                lifecycleScope.launch {
                    val result = authRepository.loginWithGoogle(idToken, account.email ?: "", account.displayName ?: "")

                    if (result.isSuccess) {
                        Toast.makeText(requireContext(), "Bienvenido, ${account.displayName}", Toast.LENGTH_SHORT).show()
                        // Aquí podemos decidir si ir al dashboard o a la verificación
                        startActivity(Intent(requireContext(), DashboardActivity::class.java))
                        activity?.finishAffinity()
                    } else {
                        Toast.makeText(requireContext(), "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } catch (e: ApiException) {
            // Error en el inicio de sesión con Google
            Toast.makeText(requireContext(), "Error al iniciar sesión con Google: ${e.statusCode}", Toast.LENGTH_LONG).show()
        }
    }
}

