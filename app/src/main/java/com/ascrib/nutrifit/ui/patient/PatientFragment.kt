package com.ascrib.nutrifit.ui.patient

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.text.HtmlCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModel
import com.ascrib.nutrifit.ui.dashboard.viewmodel.DashboardViewModelFactory
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.databinding.FragmentPatientBinding
import com.ascrib.nutrifit.handler.AppointmentHandler
import com.ascrib.nutrifit.handler.NutriologoHandler
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.model.Nutriologo
import com.ascrib.nutrifit.model.NutriologoDetalles
import com.ascrib.nutrifit.model.NutriologoInfo
import com.ascrib.nutrifit.repository.NutriologoDetailRepository
import com.ascrib.nutrifit.repository.NutriologoDetailsRepository

import com.ascrib.nutrifit.ui.dashboard.adapter.NutriologoAdapter
import com.ascrib.nutrifit.util.getStatusBarHeight
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class PatientFragment : Fragment(), NutriologoHandler {
    private lateinit var binding: FragmentPatientBinding

    private val repository = NutriologoDetailsRepository()
    private lateinit var nutriologoInfo: NutriologoInfo
    private lateinit var nutriologoDetalles:NutriologoDetalles

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_patient, container, false)
        binding.handler = this


        toolbarConfig()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Obtener el ID del nutriólogo de los argumentos
        val nutriologoId = arguments?.getInt("nutriologo_id", 0) ?: 0

        if (nutriologoId != 0) {
            loadNutriologoData(nutriologoId)
        }

    }

    private fun formatDate(dateString: String?): String {
        if (dateString.isNullOrEmpty()) return ""
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val date = inputFormat.parse(dateString)
            outputFormat.format(date)
        } catch (e: Exception) {
            dateString
        }
    }

    private fun loadNutriologoData(userId: Int) {
        lifecycleScope.launch {
            try {
                val response = repository.getNutriologoDetallesById(userId)
                if (response.isSuccessful) {
                    response.body()?.data?.let { data ->
                        nutriologoDetalles = NutriologoDetalles(
                            user_id_nutriologo = data.user_id,
                            foto = data.foto,
                            nombre_nutriologo = data.nombre_nutriologo,
                            apellido_nutriologo = data.apellido_nutriologo,
                            especialidad = data.especialidad,
                            edad = data.edad.toString()?.let { "$it años" } ?: "",
                            fecha_nacimiento = formatDate(data.fecha_nacimiento),
                            especializacion = "Especialización en: ${data.especializacion.orEmpty()}",
                            experiencia = data.experiencia?.let { "$it años" } ?: "",
                            pacientes_tratados = data.pacientes_tratados.toString() ?: "",
                            horario_antencion = formatHorarioAtencion(data.horario_antencion),
                            descripcion_nutriologo = data.descripcion_nutriologo,
                            ciudad = data.ciudad,
                            estado = data.estado,
                            genero = data.genero,
                            displomados = data.displomados,
                            telefono = data.telefono,
                            descripcion_especialziacion = data.descripcion_especialziacion,
                            universidad = data.universidad,
                            profesion = data.profesion

                        )

                        // Asignar datos al binding
                        binding.nutriologo = nutriologoDetalles
                        binding.executePendingBindings()

                        data.descripcion_especialziacion?.let { html ->
                            binding.descripcionEspecialziacion.text = HtmlCompat.fromHtml(
                                html,
                                HtmlCompat.FROM_HTML_MODE_COMPACT
                            )
                        }

                    }
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }


    private fun formatHorarioAtencion(horario: String?): String? {
        return horario?.replaceFirst(":", ":\n")?.replace(";", "\n")
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Sobre Mí"
        binding.toolbar.toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
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
                        return true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }


     override fun nutriologoClicked(nutriologo: Nutriologo) {
        findNavController().navigate(
            R.id.global_appointmentDetailFragment,
            bundleOf("nutriologo_id" to (nutriologo.user_id_nutriologo ?: 0))
        )
    }
}