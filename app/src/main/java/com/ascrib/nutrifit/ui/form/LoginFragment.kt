package com.ascrib.nutrifit.ui.form

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentLoginBinding
import com.ascrib.nutrifit.repository.AuthRepository
import com.ascrib.nutrifit.ui.dashboard.DashboardActivity
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.auth.api.signin.GoogleSignInStatusCodes
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.launch
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var authRepository: AuthRepository
    private lateinit var googleSignInClient: GoogleSignInClient

    // ActivityResultLauncher para Google Sign-In
    private lateinit var googleSignInLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Registrar ActivityResultLauncher para Google Sign-In
        googleSignInLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            Log.d("GoogleSignIn", "Resultado recibido: ${result.resultCode}")
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            handleSignInResult(task)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)
        binding.handler = this
        authRepository = AuthRepository(requireContext())
        toolbarConfig()

        // Configuración de Google Sign-In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        try {
            googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
            Log.d("GoogleSignIn", "Cliente de Google Sign-In inicializado correctamente")
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error al inicializar el cliente de Google Sign-In", e)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.layoutForm.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, requireActivity().getStatusBarHeight().plus(10), 0, 0)
        }

        // Si tienes un botón específico para Google en tu layout
        binding.googleLoginButton?.setOnClickListener {
            onGoogleSignInClicked(it)
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

    fun onLoginClicked() {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString()
        val rememberMeChecked = binding.rememberMeCheckbox.isChecked

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Guarda el estado de "Recordarme" si es necesario (puedes implementarlo aquí)

        // Ejecuta el login en una corrutina
        lifecycleScope.launch {
            val result = authRepository.login(email, password)

            if (result.isSuccess) {
                // Éxito: ir al dashboard
                Toast.makeText(requireContext(), "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                activity?.finishAffinity()
                startActivity(Intent(requireContext(), DashboardActivity::class.java))
            } else {
                // Error: mostrar mensaje
                Toast.makeText(requireContext(), "Error: ${result.exceptionOrNull()?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }



    fun onRegisterClicked() {
        findNavController().navigate(R.id.action_loginFragment_a_registerFragment)
    }

    fun onOlvidarContraClicked() {
        findNavController().navigate(R.id.action_loginFragment_a_olvidoContraFragment)
    }

    // Manejo de Google Sign-In
    fun onGoogleSignInClicked(view: View) {
        try {
            Log.d("GoogleSignIn", "Iniciando proceso de Google Sign-In")

            // Asegúrate de que el cliente de Google Sign-In se haya inicializado
            if (!::googleSignInClient.isInitialized) {
                val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()
                googleSignInClient = GoogleSignIn.getClient(requireActivity(), googleSignInOptions)
                Log.d("GoogleSignIn", "Cliente de Google Sign-In inicializado en onGoogleSignInClicked")
            }

            // Limpiar el estado anterior de inicio de sesión (opcional, pero recomendado)
            googleSignInClient.signOut().addOnCompleteListener(requireActivity()) {
                Log.d("GoogleSignIn", "Sign-out completado, iniciando nuevo Sign-In")

                // Iniciar el flujo de inicio de sesión
                val signInIntent = googleSignInClient.signInIntent
                googleSignInLauncher.launch(signInIntent)
            }
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Error al iniciar Google Sign-In", e)
            Toast.makeText(requireContext(), "Error al iniciar sesión con Google: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)

            // Imprimir información de depuración
            Log.d("GoogleSignIn", "ID Token recibido: ${account?.idToken?.substring(0, 10)}...")
            Log.d("GoogleSignIn", "Email recibido: ${account?.email}")

            // Éxito en el inicio de sesión con Google
            val idToken = account?.idToken
            if (idToken != null) {
                lifecycleScope.launch {
                    try {
                        Log.d("GoogleSignIn", "Iniciando loginWithGoogle en repository")
                        val result = authRepository.loginWithGoogle(idToken, account.email ?: "", account.displayName ?: "")

                        if (result.isSuccess) {
                            Log.d("GoogleSignIn", "Login con Google exitoso")
                            Toast.makeText(requireContext(), "Bienvenido, ${account.displayName}", Toast.LENGTH_SHORT).show()
                            startActivity(Intent(requireContext(), DashboardActivity::class.java))
                            activity?.finishAffinity()
                        } else {
                            val error = result.exceptionOrNull()
                            Log.e("GoogleSignIn", "Error en authRepository.loginWithGoogle: ${error?.message}", error)
                            Toast.makeText(requireContext(), "Error: ${error?.message ?: "Desconocido"}", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        Log.e("GoogleSignIn", "Excepción en loginWithGoogle", e)
                        Toast.makeText(requireContext(), "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Log.e("GoogleSignIn", "ID Token es nulo")
                Toast.makeText(requireContext(), "Error: No se pudo obtener el token de Google", Toast.LENGTH_LONG).show()
            }
        } catch (e: ApiException) {
            // Manejar errores específicos de Google Sign-In
            Log.e("GoogleSignIn", "ApiException en Google Sign In. Código: ${e.statusCode}", e)

            val mensaje = when (e.statusCode) {
                GoogleSignInStatusCodes.SIGN_IN_CANCELLED -> "Inicio de sesión cancelado"
                GoogleSignInStatusCodes.NETWORK_ERROR -> "Error de red"
                GoogleSignInStatusCodes.SIGN_IN_FAILED -> "Falló el inicio de sesión"
                GoogleSignInStatusCodes.SIGN_IN_REQUIRED -> "Se requiere iniciar sesión"
                else -> "Error al iniciar sesión con Google: ${e.statusCode}"
            }

            Toast.makeText(requireContext(), mensaje, Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Log.e("GoogleSignIn", "Excepción inesperada", e)
            Toast.makeText(requireContext(), "Error inesperado: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}