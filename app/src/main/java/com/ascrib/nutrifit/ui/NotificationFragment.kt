package com.ascrib.nutrifit.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.NotificacionData
import com.ascrib.nutrifit.databinding.FragmentNotificationBinding
import com.ascrib.nutrifit.model.Notificaciones
import com.ascrib.nutrifit.repository.NotificacionRepository

import com.ascrib.nutrifit.ui.dashboard.adapter.NotificacionesAdapter
import com.ascrib.nutrifit.util.getStatusBarHeight
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationFragment : Fragment() {

    private lateinit var binding: FragmentNotificationBinding
    private lateinit var notificacionesAdapter: NotificacionesAdapter
    private val repository = NotificacionRepository()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentNotificationBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.handler = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        loadNotifications()
        loadUnreadNotificationsCount()
        setupButtons()
        toolbarConfig()
    }

    // Función para mostrar Toast
    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    // Obtener el ID del paciente desde SharedPreferences
    private fun getPacienteId(): Int {
        val sharedPref = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)
        return sharedPref.getInt("Paciente_ID", 0).takeIf { it != 0 } ?: run {
            showToast("No se encontró ID de paciente")
            0
        }
    }

    private fun setupRecyclerView() {
        notificacionesAdapter = NotificacionesAdapter(emptyList()) { notificacion ->
            markNotificationAsRead(notificacion.id, notificacion.patientId ?: 0)
        }

        binding.recyclerviewNotification.apply {
            adapter = notificacionesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }


    private fun getAllPacienteIds(): List<Int> {
        val sharedPref = requireActivity().getSharedPreferences("user_data", Context.MODE_PRIVATE)

        // Obtener el ID principal (original)
        val mainPacienteId = sharedPref.getInt("Paciente_ID", 0).takeIf { it != 0 } ?: return emptyList()

        // Obtener la lista de pacientes adicionales
        val pacienteIds = try {
            sharedPref.getStringSet("paciente_ids", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        } catch (e: ClassCastException) {
            emptyList()
        }

        // Combinar todos los IDs (principal + adicionales)
        return listOf(mainPacienteId) + pacienteIds
    }



    private fun loadNotifications() {
        val pacienteIds = getAllPacienteIds().distinct() // Asegurarse de no tener duplicados
        if (pacienteIds.isEmpty()) {
            showToast("No se encontraron IDs de paciente")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val allNotifications = mutableListOf<NotificacionData>()

                for (pacienteId in pacienteIds) {
                    val response = repository.getNotificaciones(pacienteId)
                    if (response.isSuccessful && response.body()?.success == true) {
                        response.body()?.notificaciones?.let { notifs ->
                            allNotifications.addAll(notifs)
                        }
                    }
                }

                withContext(Dispatchers.Main) {
                    processNotifications(allNotifications, pacienteIds)
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error al cargar notificaciones: ${e.message}")
                }
            }
        }
    }

    private suspend fun processNotifications(allNotifications: List<NotificacionData>, pacienteIds: List<Int>) {
        if (allNotifications.isEmpty()) {
            notificacionesAdapter.updateList(emptyList())
            showToast("No hay notificaciones")
            return
        }

        val notificacionesProcesadas = mutableListOf<Notificaciones>()
        val notificacionesPorReservacion = allNotifications.groupBy { it.Reservacion_ID }

        notificacionesPorReservacion.forEach { (reservacionId, notifs) ->
            if (reservacionId != null) {
                val notifsOrdenadas = notifs.sortedByDescending { it.Notificacion_ID ?: 0 }
                val ultimaNotif = notifsOrdenadas.firstOrNull()
                val esSeguimiento = ultimaNotif?.reservacion?.estado_proximaConsulta == 3

                if (esSeguimiento) {
                    val nuevaReservacionId = buscarReservacionSeguimiento(pacienteIds.first(), reservacionId)
                    nuevaReservacionId?.let { id ->
                        allNotifications.firstOrNull { it.Reservacion_ID == id }?.let {
                            notificacionesProcesadas.add(convertirNotificacion(it))
                        }
                    }
                } else {
                    ultimaNotif?.let { notificacionesProcesadas.add(convertirNotificacion(it)) }
                    if (notifsOrdenadas.size > 1) {
                        marcarNotificacionesAnterioresComoLeidas(notifsOrdenadas.drop(1), pacienteIds.first())
                    }
                }
            }
        }

        val notificacionesNoLeidas = notificacionesProcesadas
            .filter { it.status_movil != 1 && it.estado_movil != 0 }
            .sortedByDescending { it.id }

        notificacionesAdapter.updateList(notificacionesNoLeidas)
        loadUnreadNotificationsCount()
    }

    // Función auxiliar para convertir NotificacionData a Notificaciones
    private fun convertirNotificacion(data: NotificacionData): Notificaciones {
        return Notificaciones(
            id = data.Notificacion_ID ?: 0,
            reservationId = data.Reservacion_ID,
            patientId = data.Paciente_ID,
            type = data.tipo_notificacion ?: 1,
            nutritionistName = data.nombre_nutriologo,
            rawConsultationStatus = data.reservacion?.estado_proximaConsulta,
            origen = data.reservacion?.origen,
            status_movil = data.status_movil ?: 0,
            message = data.descripcion_mensaje ?: "",
            creationDate = data.fecha_creacion ?: "",
            estado_movil = data.estado_movil ?: 1,
            user_id = data.user_id
        )
    }

    // Función para marcar notificaciones anteriores como leídas
    private fun marcarNotificacionesAnterioresComoLeidas(notificaciones: List<NotificacionData>, pacienteId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            notificaciones.forEach { notif ->
                notif.Notificacion_ID?.let { notifId ->
                    repository.marcarNotificacionLeida(notifId, pacienteId)
                }
            }
        }
    }

    // Función para buscar reservación de seguimiento
    private suspend fun buscarReservacionSeguimiento(pacienteId: Int, reservacionId: Int): Int? {
        return try {
            val response = RetrofitClient.apiService.verificarReservacionSeguimiento(reservacionId, pacienteId)
            if (response.isSuccessful && response.body()?.success == true) {
                // Buscar en las notificaciones la nueva reservación que tenga "Seguimiento:" en motivo_consulta
                val notificacionesResponse = repository.getNotificaciones(pacienteId)
                if (notificacionesResponse.isSuccessful && notificacionesResponse.body()?.success == true) {
                    notificacionesResponse.body()?.notificaciones?.firstOrNull { notif ->
                        notif.reservacion?.motivoConsulta?.startsWith("Seguimiento:") == true
                    }?.Reservacion_ID
                } else {
                    null
                }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }


    private fun loadUnreadNotificationsCount() {
        lifecycleScope.launch {
            try {
                val pacienteIds = getAllPacienteIds()
                if (pacienteIds.isEmpty()) return@launch

                var totalCount = 0
                for (pacienteId in pacienteIds) {
                    val count = withContext(Dispatchers.IO) {
                        repository.contarNotificacionesNoLeidas(pacienteId)
                    }
                    totalCount += count
                }

                NotificationBadgeUtils.updateBadgeCount(totalCount)
                requireActivity().invalidateOptionsMenu()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun markNotificationAsRead(notificacionId: Int, pacienteId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.marcarNotificacionLeida(notificacionId, pacienteId)
                withContext(Dispatchers.Main) {
                    loadNotifications() // Recargar la lista
                    loadUnreadNotificationsCount() // Actualizar contador después de marcar como leída
                    showToast("Notificación marcada como leída")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error al marcar como leída: ${e.message}")
                }
            }
        }
    }

    private fun setupButtons() {
        binding.buttonReadAll.setOnClickListener {
            markAllNotificationsAsRead()
        }

        binding.buttonClear.setOnClickListener {
            deleteAllNotifications()
        }
    }

    private fun markAllNotificationsAsRead() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pacienteIds = getAllPacienteIds()
                pacienteIds.forEach { pacienteId ->
                    repository.marcarTodasLeidas(pacienteId)
                }
                withContext(Dispatchers.Main) {
                    loadNotifications()
                    loadUnreadNotificationsCount()
                    showToast("Todas las notificaciones marcadas como leídas")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error al marcar todas como leídas: ${e.message}")
                }
            }
        }
    }


    private fun deleteAllNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val pacienteIds = getAllPacienteIds()
                pacienteIds.forEach { pacienteId ->
                    repository.eliminarNotificaciones(pacienteId)
                }
                withContext(Dispatchers.Main) {
                    notificacionesAdapter.updateList(emptyList())
                    loadNotifications()
                    loadUnreadNotificationsCount()
                    showToast("Notificaciones eliminadas")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error al eliminar notificaciones: ${e.message}")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        // Actualizar el contador al volver a este fragmento
        loadUnreadNotificationsCount()
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Mis Notificaciones"
        binding.toolbar.toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

        (requireActivity() as AppCompatActivity).apply {
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
        }

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {}

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                return when (menuItem.itemId) {
                    android.R.id.home -> {
                        findNavController().navigateUp()
                        true
                    }
                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)
    }
}