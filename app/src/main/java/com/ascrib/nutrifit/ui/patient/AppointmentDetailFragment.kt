package com.ascrib.nutrifit.ui.patient

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    companion object {
        var status = false
        private var lastNotificationCount = 0
        private var isInitialLoad = true

        // Nueva lista para mantener los conteos por paciente
        private val notificationCounts = mutableMapOf<Int, Int>()

        const val PREF_PROFILE_UPDATE_SHOWN = "profile_update_dialog_shown"
        const val DIALOG_NOT_SHOWN = 0
        const val DIALOG_SHOWN = 1
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

    private fun getAllPacienteIds(sharedPref: SharedPreferences): List<Int> {
        // 1. Obtener el ID principal
        val mainPacienteId = sharedPref.getInt("Paciente_ID", 0).takeIf { it != 0 } ?: return emptyList()

        // 2. Obtener IDs adicionales de SharedPreferences
        val additionalIds = try {
            sharedPref.getStringSet("paciente_ids", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        } catch (e: ClassCastException) {
            emptyList()
        }

        // 3. Combinar y eliminar duplicados
        return (listOf(mainPacienteId) + additionalIds).distinct()
    }

    private fun checkForNewNotifications(newCounts: Map<Int, Int>): Boolean {
        // Caso inicial (primera carga)
        if (notificationCounts.isEmpty()) return false

        // Verificar si algún paciente tiene más notificaciones que antes
        for ((pacienteId, count) in newCounts) {
            val previousCount = notificationCounts[pacienteId] ?: 0
            if (count > previousCount) {
                return true
            }
        }
        return false
    }

    private fun loadNotificationCount() {
        if (!isAdded || activity == null) return

        lifecycleScope.launch {
            try {
                val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)

                // 1. Obtener todos los IDs de paciente
                val pacienteIds = getAllPacienteIds(sharedPref)
                if (pacienteIds.isEmpty()) return@launch

                var totalCount = 0
                val newCounts = mutableMapOf<Int, Int>()

                // 2. Contar notificaciones para cada paciente
                for (pacienteId in pacienteIds) {
                    val count = withContext(Dispatchers.IO) {
                        notificacionRepository.contarNotificacionesNoLeidas(pacienteId)
                    }
                    newCounts[pacienteId] = count
                    totalCount += count
                }

                activity?.runOnUiThread {
                    // 3. Verificar si hay nuevas notificaciones
                    val hasNewNotifications = checkForNewNotifications(newCounts)

                    // 4. Reproducir sonido si hay nuevas notificaciones
                    if (hasNewNotifications && !isInitialLoad) {
                        playNotificationSound()
                    }

                    // 5. Actualizar los conteos
                    notificationCounts.clear()
                    notificationCounts.putAll(newCounts)
                    lastNotificationCount = totalCount
                    isInitialLoad = false

                    // 6. Actualizar el badge
                    NotificationBadgeUtils.updateBadgeCount(totalCount)
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

        // Verificar si ya se mostró el diálogo
        val dialogStatus = sharedPref.getInt(PREF_PROFILE_UPDATE_SHOWN, DIALOG_NOT_SHOWN)

        if (dialogStatus == DIALOG_NOT_SHOWN) {
            // Mostrar diálogo de actualización de perfil
            AlertDialog.Builder(requireContext())
                .setTitle("Actualización requerida")
                .setMessage("Debes actualizar todos tus datos en \"Mi Perfil\" para poder realizar reservaciones con el nutriólogo.")
                .setPositiveButton("Entendido") { dialog, _ ->
                    // Cambiar el estado a "mostrado"
                    sharedPref.edit().putInt(PREF_PROFILE_UPDATE_SHOWN, DIALOG_SHOWN).apply()
                    dialog.dismiss()

                    // Navegar al servicio después de aceptar
                    val nutriologoId = sharedPref.getInt("user_id_nutriologo", 0)
                    findNavController().navigate(R.id.serviceFragment, bundleOf("nutriologo_id" to nutriologoId))
                }
                .setCancelable(false)
                .show()
        } else {
            // Navegar directamente si ya se mostró el diálogo
            val nutriologoId = sharedPref.getInt("user_id_nutriologo", 0)
            findNavController().navigate(R.id.serviceFragment, bundleOf("nutriologo_id" to nutriologoId))
        }
    }


    fun onChatClicked(){
        // Opción 1: Toast simple (recomendado para tu caso)
//        Toast.makeText(
//            context,
//            "Esta sección se encuentra en mantenimiento. Disculpa las molestias.",
//            Toast.LENGTH_LONG
//        ).show()

        // Opción 2: AlertDialog más elaborado (comentado)

        AlertDialog.Builder(requireContext())
            .setTitle("Sección en Mantenimiento")
            .setMessage("La sección de chat se encuentra temporalmente en mantenimiento. Estamos trabajando para mejorar tu experiencia.")
            .setIcon(android.R.drawable.ic_dialog_info)
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()


        // Navegación original comentada
        // findNavController().navigate(R.id.global_chat_patient)
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
        AlertDialog.Builder(requireContext())
            .setTitle("Sección en Mantenimiento")
            .setMessage("Esta sección se encuentra en mantenimiento y en futuras versiones estará disponible.")
            .setPositiveButton("Entendido") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }
}