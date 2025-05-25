package com.ascrib.nutrifit.ui.dashboard

import android.content.SharedPreferences
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.updateLayoutParams
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.ConsultaData
import com.ascrib.nutrifit.databinding.FragmentEditProfileBinding
import com.ascrib.nutrifit.databinding.Profile3Binding
import com.ascrib.nutrifit.repository.NotificacionRepository
import com.ascrib.nutrifit.ui.dashboard.adapter.ReservacionesAdapter
import com.ascrib.nutrifit.ui.patient.AppointmentDetailFragment
import com.ascrib.nutrifit.ui.patient.PatientFragment
import com.ascrib.nutrifit.util.Statusbar
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileFragment : Fragment() {

    lateinit var binding : Profile3Binding
    companion object {
        var status = false
        private var lastNotificationCount = 0
        private var isInitialLoad = true

        // Nueva lista para mantener los conteos por paciente
        private val notificationCounts = mutableMapOf<Int, Int>()
    }

    private val notificacionRepository = NotificacionRepository()
    private val notificationHandler = Handler(Looper.getMainLooper())
    private var notificationRunnable: Runnable? = null
    private val pollingInterval = 15000L // 15 segundos
    private var isPollingActive = false
    private var mediaPlayer: MediaPlayer? = null

    private lateinit var reservacionesAdapter: ReservacionesAdapter
    private val reservacionesList = mutableListOf<ConsultaData>()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.profile3, container, false)

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

        // Obtener datos del usuario
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 0)
        val userName = sharedPref.getString("user_name", "")
        val userLastName = sharedPref.getString("user_lastname", "")
        val emailUser = sharedPref.getString("user_email", "")
        // Actualizar el TextView
        binding.mensageWelcome.text = " $userName $userLastName "
        binding.emailUser.text= " $emailUser"

        if (userId != 0) {
            fetchUserProfile(userId)
        }

        emailUser?.let { email ->
            if (email.isNotEmpty()) {
                fetchPacienteData(email)
            }
        }
        startNotificationPolling()

        // Solo consulta las notificaciones sin reproducir sonido en la primera carga
        // después de la creación de la vista
        loadNotificationCount()

        binding.layoutHeader.post {
            setupReservacionesRecyclerView()
            loadReservaciones()
        }
    }

    private fun setupReservacionesRecyclerView() {
        reservacionesAdapter = ReservacionesAdapter(reservacionesList)
        binding.reservacionesRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = reservacionesAdapter
            setHasFixedSize(true)
        }
    }

    private fun getPacienteIdsFromSharedPref(): List<Int> {
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val mainPacienteId = sharedPref.getInt("Paciente_ID", 0).takeIf { it != 0 } ?: return emptyList()

        val additionalIds = try {
            sharedPref.getStringSet("paciente_ids", emptySet())?.mapNotNull { it.toIntOrNull() } ?: emptyList()
        } catch (e: ClassCastException) {
            emptyList()
        }

        return (listOf(mainPacienteId) + additionalIds).distinct()
    }

    private fun getNutriologoIdsFromSharedPref(): List<Int> {
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val ids = mutableListOf<Int>()

        // 1. Obtener el ID principal del nutriólogo (guardado como Int)
        val mainNutriologoId = sharedPref.getInt("nutriologo_id", 0)
        if (mainNutriologoId != 0) {
            ids.add(mainNutriologoId)
        }

        // 2. Intentar obtener como Set<String>
        try {
            val nutriologoIdsSet = sharedPref.getStringSet("user_id_nutriologo", null)
            nutriologoIdsSet?.forEach {
                try {
                    ids.add(it.toInt())
                } catch (e: NumberFormatException) {
                    // Ignorar valores no numéricos
                }
            }
        } catch (e: ClassCastException) {
            // Si falla, intentar otras formas
        }

        // 3. Intentar obtener como String individual
        try {
            val singleIdStr = sharedPref.getString("user_id_nutriologo", null)
            singleIdStr?.toIntOrNull()?.let { ids.add(it) }
        } catch (e: ClassCastException) {
            // Si falla, continuar
        }

        // 4. Intentar obtener como Int individual (último recurso)
        try {
            val singleIdInt = sharedPref.getInt("user_id_nutriologo", 0)
            if (singleIdInt != 0) {
                ids.add(singleIdInt)
            }
        } catch (e: ClassCastException) {
            // Si falla, continuar
        }

        return ids.distinct()
    }


    private fun loadReservaciones() {
        lifecycleScope.launch {
            try {
                val pacienteIds = getPacienteIdsFromSharedPref()
                val nutriologoIds = getNutriologoIdsFromSharedPref()

                if (pacienteIds.isEmpty() || nutriologoIds.isEmpty()) {
                    return@launch
                }

                val response = RetrofitClient.apiService.getConsultasPorPaciente2(
                    pacienteIds = pacienteIds,
                    nutriologoIds = nutriologoIds
                )

                if (response.isSuccessful) {
                    response.body()?.data?.let { consultas ->
                        reservacionesList.clear()
                        reservacionesList.addAll(consultas)
                        reservacionesAdapter.notifyDataSetChanged()

                        if (consultas.isEmpty()) {
                            showNoReservacionesMessage()
                        }
                    }
                } else {
                    showErrorLoadingReservaciones()
                }
            } catch (e: Exception) {
                showErrorLoadingReservaciones()
            }
        }
    }

    private fun showNoReservacionesMessage() {
        // Puedes mostrar un mensaje cuando no hay reservaciones
        Toast.makeText(requireContext(), "No tienes citas programadas", Toast.LENGTH_SHORT).show()
    }

    private fun showErrorLoadingReservaciones() {
        Toast.makeText(requireContext(), "Error al cargar las citas", Toast.LENGTH_SHORT).show()
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

    private fun fetchPacienteData(email: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPacienteByEmail(email)
                if (response.isSuccessful) {
                    val paciente = response.body()?.paciente

                    paciente?.let {
                        val sharedPref = activity?.getSharedPreferences(
                            "user_data",
                            AppCompatActivity.MODE_PRIVATE
                        )
                        sharedPref?.edit()?.apply {
                            putInt("Paciente_ID", it.Paciente_ID ?: 0)
                            putString("user_telefono", it.telefono)
                            apply() // Don't forget to apply the changes
                        }

                        // Actualizar UI con los datos del paciente
                        binding.telefono.text = it.telefono ?: "No disponible"



                        // Cargar la foto del paciente con Glide
                        it.foto?.let { fotoUrl ->
                            Glide.with(requireContext())
                                .load(fotoUrl)
                                .placeholder(R.drawable.userdummy) // Imagen por defecto
                                .error(R.drawable.usererrror) // Imagen si hay error
                                .into(binding.foto)
                        }
                    }
                } else {
                    // Manejar error de respuesta
                    val errorBody = response.errorBody()?.string()
                    // Log.e("ProfileFragment", "Error fetching paciente: $errorBody")
                }
            } catch (e: Exception) {
                // Manejar excepciones
                // Log.e("ProfileFragment", "Error: ${e.message}")
            }
        }
    }

    private fun fetchUserProfile(userId: Int) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getProfileUser(userId)
                if (response.isSuccessful) {
                    val userProfile = response.body()?.user
                    userProfile?.let { user ->
                        // Actualizar la UI con todos los datos frescos
                        binding.mensageWelcome.text = "${user.nombre} ${user.apellidos}"
                        binding.emailUser.text = user.email

                        // Actualizar SharedPreferences con los nuevos datos
                        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("user_name", user.nombre)
                            putString("user_lastname", user.apellidos)
                            putString("user_email", user.email)
                            apply()
                        }
                    }
                } else {
                    // Manejar error de respuesta
                    val errorBody = response.errorBody()?.string()
                    // Log.e("HomeFragment", "Error fetching profile: $errorBody")
                }
            } catch (e: Exception) {
                // Manejar excepciones
                // Log.e("HomeFragment", "Error: ${e.message}")
            }
        }
    }

    private fun toolbarConfig() {
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = "Perfil"
        binding.toolbar.toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {

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

    fun onEditProfileClicked(){
        findNavController().navigate(R.id.action_profileFragment_a_editProfileFragment)
    }

}