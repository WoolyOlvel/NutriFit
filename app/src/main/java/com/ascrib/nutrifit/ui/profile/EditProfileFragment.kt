package com.ascrib.nutrifit.ui.profile

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentEditProfileBinding
import com.ascrib.nutrifit.repository.ProfileRepository
import com.ascrib.nutrifit.util.getStatusBarHeight
import android.app.Activity
import android.net.Uri
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts.*
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfileFragment : Fragment() {
    lateinit var binding: FragmentEditProfileBinding
    private lateinit var profileRepository: ProfileRepository

    private var selectedImageUri: Uri? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)
        binding.handler = this
        profileRepository = ProfileRepository(requireContext())
        toolbarConfig()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserProfile()
        loadPacienteData()
    }

    private fun loadUserProfile() {
        val sharedPref = activity?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val userId = sharedPref?.getInt("user_id", 0) ?: 0
        if (userId != 0) {
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val userProfile = profileRepository.getUserProfile(userId)
                    if (userProfile != null) {
                        binding.nombre.setText(userProfile.nombre)
                        binding.apellidos.setText(userProfile.apellidos)
                        binding.email.setText(userProfile.email)
                        binding.usuario.setText(userProfile.usuario)
                    } else {
                        Toast.makeText(context, "Error: Perfil no encontrado", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Error de conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "ID de usuario no disponible", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadPacienteData() {
        val sharedPref = activity?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val email = sharedPref?.getString("user_email", null) ?: return

        CoroutineScope(Dispatchers.Main).launch {
            try {
                val paciente = profileRepository.getPacienteByEmail(email)
                paciente?.let {
                    // Mostrar los datos en los campos correspondientes
                    binding.nombre.setText(it.nombre)
                    binding.apellidos.setText(it.apellidos)
                    binding.email.setText(it.email)
                    binding.telefono.setText(it.telefono)
                    binding.usuario.setText(it.usuario)
                    binding.edad.setText(it.edad?.toString() ?: "")
                    binding.ciudad.setText(it.ciudad)
                    binding.localidad.setText(it.localidad)
                    binding.fechaNacimiento.setText(it.fecha_nacimiento)
                    binding.enfermedad.setText(it.enfermedad)

                    // Establecer el género
                    when (it.genero) {
                        "Masculino" -> binding.genero.check(R.id.masculino)
                        "Femenino" -> binding.genero.check(R.id.femenino)
                        "Otro" -> binding.genero.check(R.id.otros)
                    }

                    // Cargar la imagen si existe
                    it.foto?.let { fotoUrl ->
                        Glide.with(requireContext())
                            .load(fotoUrl)
                            .into(binding.imageProfile)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Error al cargar datos del paciente", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Editar"
        binding.toolbar.toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.header_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }
                    R.id.action_notification -> {
                        findNavController().navigate(R.id.global_notificationFragment)
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }

    fun selectImage() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        takePhotoForResult.launch(gallery)
    }

    private val takePhotoForResult =
        registerForActivityResult(StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    selectedImageUri = uri
                    binding.imageProfile.setImageURI(uri)
                }
            }
        }

    fun saveProfile() {
        val sharedPref = activity?.getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val userId = sharedPref?.getInt("user_id", 0) ?: 0

        if (userId == 0) {
            Toast.makeText(context, "ID de usuario no disponible", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener datos del formulario
        val nombre = binding.nombre.text.toString()
        val apellidos = binding.apellidos.text.toString()
        val email = binding.email.text.toString()
        val usuario = binding.usuario.text.toString()
        val telefono = binding.telefono.text.toString()
        val edad = binding.edad.text.toString().toIntOrNull() ?: 0
        val ciudad = binding.ciudad.text.toString()
        val localidad = binding.localidad.text.toString()
        val fechaNacimiento = binding.fechaNacimiento.text.toString()
        val enfermedad = binding.enfermedad.text.toString()

        // Validar campos obligatorios
        if (nombre.isEmpty() || apellidos.isEmpty() || email.isEmpty() || usuario.isEmpty()) {
            Toast.makeText(context, "Por favor complete todos los campos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        // Obtener género seleccionado
        val genero = when (binding.genero.checkedRadioButtonId) {
            R.id.masculino -> "Masculino"
            R.id.femenino -> "Femenino"
            R.id.otros -> "Otro"
            else -> {
                Toast.makeText(context, "Por favor seleccione un género", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // En saveProfile()
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // 1. Actualizar datos del usuario
                println("DEBUG - Actualizando perfil de usuario...")
                val updateSuccess = profileRepository.updateUserProfile(
                    userId,
                    nombre,
                    apellidos,
                    email,
                    usuario
                )

                if (!updateSuccess) {
                    Toast.makeText(context, "Error al actualizar perfil", Toast.LENGTH_SHORT).show()
                    return@launch
                }

                // 2. Actualizar datos del paciente
                val paciente = profileRepository.getPacienteByEmail(email)
                paciente?.let {
                    println("DEBUG - Actualizando datos del paciente...")
                    val updatePacienteSuccess = profileRepository.updatePacienteByEmail(
                        email,
                        selectedImageUri,
                        it.foto, // current foto URL
                        nombre,
                        apellidos,
                        telefono,
                        genero,
                        usuario,
                        enfermedad,
                        ciudad,
                        localidad,
                        edad,
                        fechaNacimiento
                    )

                    if (updatePacienteSuccess) {
                        Toast.makeText(context, "Perfil actualizado exitosamente", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(context, "Error al actualizar datos del paciente", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    // Si no existe paciente, crear uno nuevo
                    println("DEBUG - Creando nuevo paciente...")
                    val createSuccess = profileRepository.createPaciente(
                        selectedImageUri,
                        nombre,
                        apellidos,
                        email,
                        telefono,
                        genero,
                        usuario,
                        2, // rol_id = 2 (paciente)
                        enfermedad,
                        ciudad,
                        localidad,
                        edad,
                        fechaNacimiento
                    )

                    if (createSuccess) {
                        Toast.makeText(context, "Perfil guardado exitosamente", Toast.LENGTH_SHORT).show()
                        findNavController().navigateUp()
                    } else {
                        Toast.makeText(context, "Error al crear paciente", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                println("DEBUG - Error general: ${e.message}")
                e.printStackTrace()
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}