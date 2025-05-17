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
import com.ascrib.nutrifit.databinding.DialogAnswerBinding
import com.ascrib.nutrifit.databinding.FragmentAppointmentDetailBinding
import com.ascrib.nutrifit.databinding.FragmentPatientBinding
import com.ascrib.nutrifit.handler.AppointmentHandler
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.model.NutriologoInfo
import com.ascrib.nutrifit.repository.NutriologoDetailRepository
//import com.ascrib.nutrifit.ui.dashboard.adapter.AppointmentAdapter
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import androidx.databinding.BindingAdapter
import com.ascrib.nutrifit.model.Nutriologo
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentDetailFragment : Fragment() {
    private lateinit var binding: FragmentAppointmentDetailBinding
    private val repository = NutriologoDetailRepository()
    private lateinit var nutriologoInfo: NutriologoInfo


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_appointment_detail,
            container,
            false
        )
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

        binding.cardReview.visibility = View.VISIBLE
        //binding.textCancel.visibility = View.VISIBLE
        binding.cardConsult.visibility = View.VISIBLE
        //binding.layoutButtons.visibility = View.VISIBLE
        //binding.textCompleted.visibility = View.VISIBLE

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
                val response = repository.getNutriologoById(userId)
                if (response.isSuccessful) {
                    response.body()?.data?.let { data ->
                        nutriologoInfo = NutriologoInfo(
                            user_id_nutriologo = data.user_id,
                            foto = data.foto,
                            nombre_nutriologo = data.nombre_nutriologo,
                            apellido_nutriologo = data.apellido_nutriologo,
                            modalidad = data.modalidad,
                            disponibilidad = data.disponibilidad,
                            especialidad = data.especialidad,
                            edad = data.edad.toString()?.let { "$it años" } ?: "",
                            fecha_nacimiento = formatDate(data.fecha_nacimiento),
                            especializacion = data.especializacion,
                            experiencia = data.experiencia?.let { "$it años" } ?: "",
                            pacientes_tratados = data.pacientes_tratados.toString() ?: "",
                            horario_antencion = formatHorarioAtencion(data.horario_antencion),
                            descripcion_nutriologo = data.descripcion_nutriologo,
                            ciudad = data.ciudad,
                            estado = data.estado,
                            genero = data.genero,
                            enfermedades_tratadas = data.enfermedades_tratadas
                        )

                        // Asignar datos al binding
                        binding.nutriologo = nutriologoInfo
                        binding.executePendingBindings()

                        // Manejar HTML en enfermedades tratadas
                        data.enfermedades_tratadas?.let { html ->
                            binding.enfermedadesTratadas.text = HtmlCompat.fromHtml(
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

        binding.toolbar.toolbar.title = "Reservar Cita"
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

    fun onProfileClicked(nutriologo: NutriologoInfo){
        findNavController().navigate(R.id.global_patientFragment,
            bundleOf("nutriologo_id" to (nutriologo.user_id_nutriologo ?: 0)))

    }

    fun onCallClicked() {
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val nutriologoId = sharedPref.getInt("user_id_nutriologo", 0)
        findNavController().navigate(R.id.serviceFragment, bundleOf("nutriologo_id" to nutriologoId))
    }

    fun onChatClicked(){
        findNavController().navigate(R.id.global_chat_patient)
    }

    private fun showBottomSheetReply() {
        val dialog = BottomSheetDialog(requireContext())

        val sheetBinding: DialogAnswerBinding =
            DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.dialog_answer,
                null,
                false
            )
        dialog.behavior.state = BottomSheetBehavior.STATE_EXPANDED
        sheetBinding.imageClose.setOnClickListener {
            dialog.dismiss()
        }
        dialog.setContentView(sheetBinding.root)
        dialog.show()
    }

    fun onAnswerClicked() {
        showBottomSheetReply()
    }
}