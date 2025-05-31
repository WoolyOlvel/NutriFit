package com.ascrib.nutrifit.ui.dashboard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.ascrib.nutrifit.R
import com.ascrib.nutrifit.api.models.ConsultaData
import com.ascrib.nutrifit.databinding.ItemReservacionBinding
import com.bumptech.glide.Glide
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReservacionesAdapter(private val reservaciones: List<ConsultaData>) :
    RecyclerView.Adapter<ReservacionesAdapter.ReservacionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReservacionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemReservacionBinding.inflate(inflater, parent, false)
        return ReservacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReservacionViewHolder, position: Int) {
        holder.bind(reservaciones[position])
    }

    override fun getItemCount() = reservaciones.size

    inner class ReservacionViewHolder(private val binding: ItemReservacionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(reservacion: ConsultaData) {
            // Formatear la fecha
            val formattedDate = formatDate(reservacion.fecha_consulta)
            binding.fechaConsulta.text = formattedDate

            // Mostrar información del nutriólogo
            reservacion.nombre_nutriologo?.let { nombreNutriologo ->
                binding.nutriologoName.text = "Con el Nut. $nombreNutriologo"
            }

            // Cargar imagen del nutriólogo si está disponible
            reservacion.foto_nutriologo?.let { fotoUrl ->
                // Reemplazar localhost por la IP correcta
                val correctedUrl = fotoUrl.replace("http://127.0.0.1:8000", "nutrifitplanner.site")
                    .replace("http://", "https://")

                Glide.with(binding.root.context)
                    .load(correctedUrl)
                    .placeholder(R.drawable.userdummy)
                    .error(R.drawable.usererrror)
                    .into(binding.nutriologoImage)
            } ?: run {
                binding.nutriologoImage.setImageResource(R.drawable.userdummy)
            }
        }

        // En ReservacionesAdapter.kt
        private fun formatDate(dateString: String?): String {
            if (dateString.isNullOrEmpty()) return "Fecha no disponible"

            return try {
                // Primero intenta con formato ISO (con 'T' y 'Z')
                try {
                    val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    val date = isoFormat.parse(dateString)
                    return formatDateToSpanish(date)
                } catch (e: Exception) {
                    // Si falla, intenta con formato simple
                    val simpleFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                    val date = simpleFormat.parse(dateString)
                    return formatDateToSpanish(date)
                }
            } catch (e: Exception) {
                // Si todo falla, muestra la fecha original para depuración
                "Fecha: $dateString"
            }
        }

        private fun formatDateToSpanish(date: Date): String {
            val outputFormat = SimpleDateFormat("EEEE, d 'de' MMMM 'del' yyyy\n'a las' hh:mm a", Locale("es", "ES"))
            return outputFormat.format(date)
        }
    }
}