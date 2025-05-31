package com.ascrib.nutrifit.ui.form

import android.os.Bundle
import android.text.Editable
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ScrollView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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
    private val passwordPatterns = listOf(
        ".{8,}" to "Al menos 8 caracteres",
        ".*[A-Z].*" to "Al menos una mayúscula",
        ".*[a-z].*" to "Al menos una minúscula",
        ".*\\d.*" to "Al menos un número"
    )
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
        setupKeyboardHandling()
        setupPasswordValidation()

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

    private fun setupKeyboardHandling() {
        // 1. Listener para detectar cambios en el layout (cuando aparece/desaparece el teclado)
        binding.root.viewTreeObserver.addOnGlobalLayoutListener {
            handleKeyboardVisibility()
        }

        // 2. Configurar el ScrollView para que sea más responsivo
        binding.scrollView.apply {
            isSmoothScrollingEnabled = true
            isScrollbarFadingEnabled = false
            scrollBarStyle = View.SCROLLBARS_OUTSIDE_OVERLAY
        }

        // 3. Agregar listeners a todos los EditText para manejar el focus
        setupAllEditTextListeners()
    }

    private fun handleKeyboardVisibility() {
        val rootView = binding.root
        val scrollView = binding.scrollView

        // Calcular si el teclado está visible
        val rect = android.graphics.Rect()
        rootView.getWindowVisibleDisplayFrame(rect)
        val screenHeight = rootView.height
        val keypadHeight = screenHeight - rect.bottom

        if (keypadHeight > screenHeight * 0.15) {
            // Teclado está visible
            onKeyboardShown(keypadHeight)
        } else {
            // Teclado está oculto
            onKeyboardHidden()
        }
    }

    private fun onKeyboardShown(keyboardHeight: Int) {
        // Ajustar el ScrollView para que tenga espacio suficiente
        binding.scrollView.apply {
            setPadding(paddingLeft, paddingTop, paddingRight, keyboardHeight / 4)
            post {
                // Scroll hacia el campo enfocado si es necesario
                val focusedView = activity?.currentFocus
                focusedView?.let { view ->
                    scrollToView(view)
                }
            }
        }
    }

    private fun onKeyboardHidden() {
        // Restablecer el padding del ScrollView
        binding.scrollView.setPadding(
            binding.scrollView.paddingLeft,
            binding.scrollView.paddingTop,
            binding.scrollView.paddingRight,
            0
        )
    }

    private fun setupAllEditTextListeners() {
        val editTexts = listOf(
            binding.nombreEditText,
            binding.apellidosEditText,
            binding.emailEditText,
            binding.usuarioEditText,
            binding.passwordEditText
        )

        editTexts.forEach { editText ->
            editText.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    // Delay para asegurar que el teclado ya apareció
                    view.postDelayed({
                        scrollToView(view)
                    }, 300)
                }
            }
        }
    }

    private fun scrollToView(view: View) {
        val scrollView = binding.scrollView
        val scrollBounds = android.graphics.Rect()
        scrollView.getHitRect(scrollBounds)

        if (!view.getLocalVisibleRect(scrollBounds)) {
            // La vista no está completamente visible, hacer scroll
            val coords = IntArray(2)
            view.getLocationInWindow(coords)

            val scrollViewCoords = IntArray(2)
            scrollView.getLocationInWindow(scrollViewCoords)

            val scrollToY = coords[1] - scrollViewCoords[1] - 200 // Margen de 200px
            scrollView.smoothScrollTo(0, scrollToY)
        }
    }

    private fun setupPasswordValidation() {
        binding.passwordEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                val password = s.toString()
                updatePasswordRequirements(password)
            }
        })
    }

    private fun updatePasswordRequirements(password: String) {
        val requirements = passwordPatterns.map { (pattern, message) ->
            val color = if (password.matches(pattern.toRegex())) {
                ContextCompat.getColor(requireContext(), R.color.colorPrimary)
            } else {
                ContextCompat.getColor(requireContext(), R.color.grey)
            }
            SpannableString(message).apply {
                setSpan(ForegroundColorSpan(color), 0, length, Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            }
        }

        val spannable = SpannableStringBuilder().apply {
            requirements.forEachIndexed { index, requirement ->
                append(requirement)
                if (index < requirements.size - 1) append("\n")
            }
        }

        binding.passwordRequirements.text = spannable
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


