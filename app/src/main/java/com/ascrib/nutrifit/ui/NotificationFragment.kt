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

    private fun loadNotifications() {
        val pacienteId = getPacienteId()
        if (pacienteId == 0) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = repository.getNotificaciones(pacienteId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val notificacionesData = response.body()?.notificaciones ?: emptyList()

                        // Procesar notificaciones agrupadas por Reservacion_ID
                        val notificacionesProcesadas = mutableListOf<Notificaciones>()
                        val notificacionesPorReservacion = notificacionesData.groupBy { it.Reservacion_ID }

                        notificacionesPorReservacion.forEach { (reservacionId, notifs) ->
                            if (reservacionId != null) {
                                // Ordenar por ID descendente para tener la más reciente primero
                                val notifsOrdenadas = notifs.sortedByDescending { it.Notificacion_ID ?: 0 }

                                // Tomar la notificación más reciente
                                val ultimaNotif = notifsOrdenadas.firstOrNull()

                                // Verificar si es una reservación de seguimiento (estado 3)
                                val esSeguimiento = ultimaNotif?.reservacion?.estado_proximaConsulta == 3

                                if (esSeguimiento) {
                                    // Buscar la notificación de la nueva reservación de seguimiento
                                    val nuevaReservacionId = buscarReservacionSeguimiento(pacienteId, reservacionId)
                                    if (nuevaReservacionId != null) {
                                        val notifSeguimiento = notificacionesData.firstOrNull {
                                            it.Reservacion_ID == nuevaReservacionId
                                        }
                                        notifSeguimiento?.let { notificacionesProcesadas.add(convertirNotificacion(it)) }
                                    }
                                } else {
                                    // Para casos normales, tomar solo la más reciente
                                    ultimaNotif?.let { notificacionesProcesadas.add(convertirNotificacion(it)) }

                                    // Marcar las anteriores como leídas
                                    if (notifsOrdenadas.size > 1) {
                                        marcarNotificacionesAnterioresComoLeidas(
                                            notifsOrdenadas.drop(1),
                                            pacienteId
                                        )
                                    }
                                }
                            }
                        }

                        // Filtrar solo no leídas y actualizar el adaptador
                        val notificacionesNoLeidas = notificacionesProcesadas
                            .filter { it.status_movil != 1 && it.estado_movil != 0 } // Filtra por ambos campos
                            .sortedByDescending { it.id }

                        notificacionesAdapter.updateList(notificacionesNoLeidas)
                        loadUnreadNotificationsCount()
                    } else {
                        showToast("Error al cargar notificaciones")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error al cargar notificaciones: ${e.message}")
                }
            }
        }
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
                val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                val pacienteId = sharedPref.getInt("Paciente_ID", 0)
                if (pacienteId == 0) return@launch

                val count = withContext(Dispatchers.IO) {
                    repository.contarNotificacionesNoLeidas(pacienteId)
                }

                NotificationBadgeUtils.updateBadgeCount(count)

                // Actualizar opciones de menú para refrescar el badge
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
            val pacienteId = getPacienteId()
            if (pacienteId != 0) markAllNotificationsAsRead(pacienteId)
        }

        binding.buttonClear.setOnClickListener {
            val pacienteId = getPacienteId()
            if (pacienteId != 0) deleteAllNotifications(pacienteId)
        }
    }

    private fun markAllNotificationsAsRead(pacienteId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.marcarTodasLeidas(pacienteId)
                withContext(Dispatchers.Main) {
                    loadNotifications()
                    loadUnreadNotificationsCount() // Actualizar contador después de marcar todas como leídas
                    showToast("Todas las notificaciones marcadas como leídas")
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    showToast("Error al marcar todas como leídas: ${e.message}")
                }
            }
        }
    }

    private fun deleteAllNotifications(pacienteId: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.eliminarNotificaciones(pacienteId)
                withContext(Dispatchers.Main) {
                    notificacionesAdapter.updateList(emptyList())
                    loadNotifications()
                    loadUnreadNotificationsCount() // Actualizar contador después de eliminar
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