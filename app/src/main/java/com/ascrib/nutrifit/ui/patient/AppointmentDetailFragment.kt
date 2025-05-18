package com.ascrib.nutrifit.ui.patient

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.ascrib.nutrifit.repository.NotificacionRepository
import com.ascrib.nutrifit.ui.dashboard.ScheduleFragment
import com.ascrib.nutrifit.ui.dashboard.ScheduleFragment.schedule
import com.ascrib.nutrifit.ui.dashboard.adapter.NutriologoAdapter
import com.ascrib.nutrifit.util.Statusbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentDetailFragment : Fragment() {
    private lateinit var binding: FragmentAppointmentDetailBinding
    private val repository = NutriologoDetailRepository()
    private lateinit var nutriologoInfo: NutriologoInfo
    companion object schedule {
        var status = false
        // Añadir variable estática para mantener el conteo entre instancias del fragmento
        private var lastNotificationCount = 0
        // Añadir bandera para indicar si es la primera carga del fragmento en la sesión
        private var isInitialLoad = true
    }

    private lateinit var nutriologoAdapter: NutriologoAdapter
    private var nutriologosList: List<Nutriologo> = listOf()
    private val notificacionRepository = NotificacionRepository()
    private val notificationHandler = Handler(Looper.getMainLooper())
    private var notificationRunnable: Runnable? = null
    private val pollingInterval = 15000L // 15 segundos
    private var isPollingActive = false
    private var mediaPlayer: MediaPlayer? = null


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
        Statusbar.setStatusbarTheme(
            requireContext(),
            requireActivity().window,
            R.color.lightGrey,
            binding.root
        )
        mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil)
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

        startNotificationPolling()

        // Solo consulta las notificaciones sin reproducir sonido en la primera carga
        // después de la creación de la vista
        loadNotificationCount()

    }

    private fun startNotificationPolling() {
        if (isPollingActive) return

        isPollingActive = true
        notificationRunnable = object : Runnable {
            override fun run() {
                loadNotificationCount()
                notificationHandler.postDelayed(this, pollingInterval)
            }
        }
        notificationHandler.post(notificationRunnable!!)
    }

    private fun stopNotificationPolling() {
        isPollingActive = false
        notificationRunnable?.let {
            notificationHandler.removeCallbacks(it)
        }
    }

    private fun loadNotificationCount() {
        if (!isAdded || activity == null) return

        lifecycleScope.launch {
            try {
                val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                val pacienteId = sharedPref.getInt("Paciente_ID", 0)
                if (pacienteId == 0) return@launch

                val count = withContext(Dispatchers.IO) {
                    notificacionRepository.contarNotificacionesNoLeidas(pacienteId)
                }

                activity?.runOnUiThread {
                    // Reproducir sonido solo si hay nuevas notificaciones
                    // Y no es la carga inicial del fragmento
                    if (count > AppointmentDetailFragment.lastNotificationCount && !AppointmentDetailFragment.isInitialLoad) {
                        playNotificationSound()
                    }

                    // Actualizar el contador estático
                    AppointmentDetailFragment.lastNotificationCount = count

                    // Actualizar la bandera después de la primera carga
                    AppointmentDetailFragment.isInitialLoad = false

                    NotificationBadgeUtils.updateBadgeCount(count)
                    requireActivity().invalidateOptionsMenu()
                }
            } catch (e: Exception) {
                // Reintentar más rápido si hay error
                if (isPollingActive) {
                    notificationHandler.postDelayed({ loadNotificationCount() }, 5000)
                }
            }
        }
    }

    private fun playNotificationSound() {
        try {
            if (mediaPlayer == null) {
                mediaPlayer = MediaPlayer.create(context, R.raw.notificacion_movil).apply {
                    setOnCompletionListener {
                        it.release()
                        mediaPlayer = null
                    }
                }
            }
            mediaPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Liberar recursos del MediaPlayer
        stopNotificationPolling()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onPause() {
        super.onPause()
        stopNotificationPolling()
    }

    // Reinicia el desplazamiento automático cuando el fragmento vuelve a ser visible
    override fun onResume() {
        super.onResume()
        if (!isPollingActive) {
            startNotificationPolling()
        }

        // Carga inmediata de notificaciones cuando vuelve el fragmento
        loadNotificationCount()
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
                menu.findItem(R.id.action_notification)?.let { menuItem ->
                    NotificationBadgeUtils.setupBadge(requireActivity() as AppCompatActivity, menuItem)
                }
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