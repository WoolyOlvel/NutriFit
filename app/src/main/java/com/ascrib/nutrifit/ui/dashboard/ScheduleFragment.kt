package com.ascrib.nutrifit.ui.dashboard

import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
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
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.databinding.FragmentScheduleBinding
import com.ascrib.nutrifit.handler.AppointmentHandler
import com.ascrib.nutrifit.handler.NutriologoHandler
import com.ascrib.nutrifit.model.Appointment
import com.ascrib.nutrifit.model.Nutriologo
import com.ascrib.nutrifit.ui.dashboard.adapter.NutriologoAdapter
import com.ascrib.nutrifit.util.getStatusBarHeight
import com.bumptech.glide.Glide
import com.prolificinteractive.materialcalendarview.CalendarMode
import kotlinx.coroutines.launch

class ScheduleFragment : Fragment(), NutriologoHandler {
    companion object schedule {
        var status = false
    }

    lateinit var binding: FragmentScheduleBinding
    lateinit var model: DashboardViewModel

    private lateinit var nutriologoAdapter: NutriologoAdapter
    private var nutriologosList: List<Nutriologo> = listOf()



    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_schedule, container, false)

        toolbarConfig()

        binding.handler = this



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Obtener datos del usuario
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", 0)
        val userName = sharedPref.getString("user_name", "")
        val emailUser = sharedPref.getString("user_email", "")

        if (userId != 0) {
            fetchUserProfile(userId)
        }

        emailUser?.let { email ->
            if (email.isNotEmpty()) {
                fetchPacienteData(email)
            }
        }

        // Actualizar el TextView
        binding.mensageWelcome.text = "Hola $userName \n¡Reserva una cita ahora!"


        calendarSetup()
        loadNutriologos()
    }

    private fun fetchPacienteData(email: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getPacienteByEmail(email)
                if (response.isSuccessful) {
                    val paciente = response.body()?.paciente
                    paciente?.let {
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
                        // Actualizar la UI con los datos frescos
                        binding.mensageWelcome.text = "Hola ${user.nombre} \n¡Reserva una cita ahora!"

                        // Opcional: Actualizar SharedPreferences con los nuevos datos
                        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
                        with(sharedPref.edit()) {
                            putString("user_name", user.nombre)
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

    fun calendarSetup() { //Este no tocar por el momento
        binding.calendarView.setOnTitleClickListener(View.OnClickListener {
            if (status) {
                binding.calendarView.state().edit().setCalendarDisplayMode(CalendarMode.WEEKS)
                    .commit()
                status = false
            } else {
                binding.calendarView.state().edit().setCalendarDisplayMode(CalendarMode.MONTHS)
                    .commit()
                status = true
            }
        })

        binding.calendarView.state().edit().isCacheCalendarPositionEnabled(true)
    }

    private fun toolbarConfig() { //Este no tocar por el momento
        binding.toolbar.toolbar.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.layoutHeader.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            setMargins(0, activity?.getStatusBarHeight()!!.plus(10), 0, 0)
        }

        binding.toolbar.toolbar.title = ""
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.toolbar.toolbar)

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

    private fun loadNutriologos() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.apiService.getNutriologos()
                if (response.isSuccessful) {
                    nutriologosList = response.body()?.data?.map { nutriologoData ->
                        Nutriologo(
                            user_id_nutriologo = nutriologoData.user_id,
                            foto = nutriologoData.foto,
                            nombre_nutriologo = nutriologoData.nombre_nutriologo,
                            apellido_nutriologo = nutriologoData.apellido_nutriologo,
                            modalidad = nutriologoData.modalidad,
                            disponibilidad = nutriologoData.disponibilidad,
                            especialidad = nutriologoData.especialidad
                        )
                    } ?: listOf()

                    setupNutriologosRecyclers()
                }
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }

    private fun setupNutriologosRecyclers() {
        // Todos los nutriólogos
        nutriologoAdapter = NutriologoAdapter(nutriologosList, this)
        binding.recyclerviewAppointments.apply {
            adapter = nutriologoAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        // Nutriólogos disponibles
        val disponiblesAdapter = NutriologoAdapter(
            nutriologosList.filter { it.disponibilidad.equals("Disponibles", ignoreCase = true) },
            this
        )
        binding.recyclerviewInProgress.apply {
            adapter = disponiblesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        // Nutriólogos con pocos cupos
        val pocosCuposAdapter = NutriologoAdapter(
            nutriologosList.filter { it.disponibilidad.equals("Pocos Cupos", ignoreCase = true) },
            this
        )
        binding.recyclerviewNextConsults.apply {
            adapter = pocosCuposAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }

        // Nutriólogos no disponibles
        val noDisponiblesAdapter = NutriologoAdapter(
            nutriologosList.filter { it.disponibilidad.equals("No Disponible", ignoreCase = true) },
            this
        )
        binding.recyclerviewPastConsults.apply {
            adapter = noDisponiblesAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        }
    }

    override fun nutriologoClicked(nutriologo: Nutriologo) {
        // Guardar el user_id_nutriologo en SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("user_data", AppCompatActivity.MODE_PRIVATE)
        sharedPref.edit().putInt("user_id_nutriologo", nutriologo.user_id_nutriologo ?: 0).apply()

        // Navegar al detalle o realizar otra acción
        findNavController().navigate(
            R.id.global_appointmentDetailFragment,
            bundleOf("nutriologo_id" to (nutriologo.user_id_nutriologo ?: 0))
        )
    }

} 