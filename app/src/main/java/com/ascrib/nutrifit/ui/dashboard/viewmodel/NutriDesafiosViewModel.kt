package com.ascrib.nutrifit.ui.dashboard.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ascrib.nutrifit.api.ApiService
import com.ascrib.nutrifit.api.models.NutriDesafiosResponse
import com.ascrib.nutrifit.model.NutriDesafios
import kotlinx.coroutines.launch
import javax.inject.Inject

class NutriDesafiosViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _desafios = MutableLiveData<List<NutriDesafios>>()
    val desafios: LiveData<List<NutriDesafios>> = _desafios

    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchNutriDesafios() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val response = apiService.getNutriDesafios()
                if (response.isSuccessful && response.body()?.success == true) {
                    val desafiosResponse = response.body()?.data ?: emptyList()
                    _desafios.value = desafiosResponse.map { it.toModel() }
                } else {
                    _error.value = "Error al obtener los desafíos"
                }
            } catch (e: Exception) {
                _error.value = "Error de conexión"
            } finally {
                _loading.value = false
            }
        }
    }

    private fun NutriDesafiosResponse.toModel(): NutriDesafios {
        return NutriDesafios(
            NutriDesafios_ID = this.NutriDesafios_ID,
            foto = this.foto,
            nombre = this.nombre,
            url = this.url,
            tipo = this.tipo,
            status = this.status,
            estado = this.estado
        )
    }
}