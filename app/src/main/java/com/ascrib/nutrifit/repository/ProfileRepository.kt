package com.ascrib.nutrifit.repository

import android.content.Context
import android.net.Uri
import androidx.core.net.toFile
import com.ascrib.nutrifit.api.RetrofitClient
import com.ascrib.nutrifit.api.models.Paciente
import com.ascrib.nutrifit.api.models.ProfileUserData
import com.ascrib.nutrifit.api.models.UpdatePacienteRequest
import com.ascrib.nutrifit.api.models.UpdateProfileRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.InputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*

class ProfileRepository(private val context: Context?) {

    suspend fun getUserProfile(userId: Int): ProfileUserData? {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getProfileUser(userId)
                if (response.isSuccessful) {
                    response.body()?.user
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun updateUserProfile(
        userId: Int,
        nombre: String,
        apellidos: String,
        email: String,
        usuario: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val request = UpdateProfileRequest(nombre, apellidos, email, usuario)
                val response = RetrofitClient.apiService.updateUserProfile(userId, request)
                response.isSuccessful
            } catch (e: Exception) {
                false
            }
        }
    }

    suspend fun createPaciente(
        fotoUri: Uri?,
        nombre: String,
        apellidos: String,
        email: String,
        telefono: String,
        genero: String,
        usuario: String,
        rolId: Int,
        enfermedad: String,
        ciudad: String,
        localidad: String,
        edad: Int,
        fechaNacimiento: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG - Preparando datos para crear paciente...")

                // 1. Manejo de la imagen
                var fotoPart: MultipartBody.Part? = null
                fotoUri?.let { uri ->
                    println("DEBUG - Procesando imagen desde URI: $uri")
                    try {
                        // Crear archivo temporal
                        val tempFile = createTempFile(context, uri)
                        val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
                        fotoPart = MultipartBody.Part.createFormData(
                            "foto",
                            "profile_${System.currentTimeMillis()}.jpg",
                            requestFile
                        )
                        println("DEBUG - Imagen preparada correctamente")
                    } catch (e: Exception) {
                        println("DEBUG - Error al procesar imagen: ${e.message}")
                        e.printStackTrace()
                    }
                }

                // 2. Preparar fecha de creación
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val fechaCreacion = sdf.format(Date())

                // 3. Crear RequestBody para cada campo
                val requestBodies = mapOf(
                    "nombre" to nombre.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "apellidos" to apellidos.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "email" to email.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "telefono" to telefono.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "genero" to genero.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "usuario" to usuario.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "rol_id" to rolId.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    "enfermedad" to enfermedad.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "status" to "1".toRequestBody("text/plain".toMediaTypeOrNull()),
                    "estado" to "1".toRequestBody("text/plain".toMediaTypeOrNull()),
                    "ciudad" to ciudad.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "localidad" to localidad.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "edad" to edad.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
                    "fecha_nacimiento" to fechaNacimiento.toRequestBody("text/plain".toMediaTypeOrNull()),
                    "fecha_creacion" to fechaCreacion.toRequestBody("text/plain".toMediaTypeOrNull())
                )

                // 4. Enviar petición
                println("DEBUG - Enviando petición...")
                val response = RetrofitClient.apiService.createPaciente(
                    fotoPart,
                    requestBodies["nombre"]!!,
                    requestBodies["apellidos"]!!,
                    requestBodies["email"]!!,
                    requestBodies["telefono"]!!,
                    requestBodies["genero"]!!,
                    requestBodies["usuario"]!!,
                    requestBodies["rol_id"]!!,
                    requestBodies["enfermedad"]!!,
                    requestBodies["status"]!!,
                    requestBodies["estado"]!!,
                    requestBodies["ciudad"]!!,
                    requestBodies["localidad"]!!,
                    requestBodies["edad"]!!,
                    requestBodies["fecha_nacimiento"]!!,
                    requestBodies["fecha_creacion"]!!
                )

                // 5. Manejar respuesta
                println("DEBUG - Respuesta recibida: ${response.code()}")
                if (!response.isSuccessful) {
                    val errorBody = response.errorBody()?.string()
                    println("DEBUG - Error en la respuesta: $errorBody")
                }

                response.isSuccessful
            } catch (e: Exception) {
                println("DEBUG - Excepción al crear paciente: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    private fun createTempFile(context: Context?, uri: Uri): File {
        return File(context?.cacheDir, "temp_image_${System.currentTimeMillis()}.jpg").apply {
            context?.contentResolver?.openInputStream(uri)?.use { inputStream ->
                outputStream().use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        }
    }

    suspend fun getPacienteByEmail(email: String): Paciente? {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getPacienteByEmail(email)
                if (response.isSuccessful) {
                    response.body()?.paciente
                } else {
                    println("DEBUG - Error al obtener paciente: ${response.errorBody()?.string()}")
                    null
                }
            } catch (e: Exception) {
                println("DEBUG - Excepción al obtener paciente: ${e.message}")
                null
            }
        }
    }

    suspend fun updatePacienteByEmail(
        email: String,
        fotoUri: Uri?,
        currentFotoUrl: String?,
        nombre: String,
        apellidos: String,
        telefono: String,
        genero: String,
        usuario: String,
        enfermedad: String,
        ciudad: String,
        localidad: String,
        edad: Int,
        fechaNacimiento: String
    ): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                println("DEBUG - Iniciando actualización para email: $email")

                if (fotoUri != null) {
                    println("DEBUG - Procesando nueva imagen...")

                    // Crear la parte de la imagen
                    val fotoPart = try {
                        createImagePart(fotoUri).also {
                            println("DEBUG - Parte de imagen creada correctamente")
                        }
                    } catch (e: Exception) {
                        println("DEBUG - Error al crear parte de imagen: ${e.message}")
                        return@withContext false
                    }

                    // Crear todos los RequestBody necesarios
                    val requestBodies = listOf(
                        createPartFromString("nombre", nombre),
                        createPartFromString("apellidos", apellidos),
                        createPartFromString("telefono", telefono),
                        createPartFromString("genero", genero),
                        createPartFromString("usuario", usuario),
                        createPartFromString("enfermedad", enfermedad),
                        createPartFromString("ciudad", ciudad),
                        createPartFromString("localidad", localidad),
                        createPartFromString("edad", edad.toString()),
                        createPartFromString("fecha_nacimiento", fechaNacimiento)
                    )

                    println("DEBUG - Enviando solicitud multipart...")
                    val response = RetrofitClient.apiService.updatePacienteWithPhotoByEmail(
                        email = email,
                        foto = fotoPart,
                        nombre = requestBodies[0],
                        apellidos = requestBodies[1],
                        telefono = requestBodies[2],
                        genero = requestBodies[3],
                        usuario = requestBodies[4],
                        enfermedad = requestBodies[5],
                        ciudad = requestBodies[6],
                        localidad = requestBodies[7],
                        edad = requestBodies[8],
                        fecha_nacimiento = requestBodies[9]
                    )

                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string()
                        println("DEBUG - Error en la respuesta (con foto): $errorBody")
                        return@withContext false
                    }

                    println("DEBUG - Actualización con foto exitosa")
                    true
                } else {
                    println("DEBUG - Actualizando sin nueva imagen...")
                    val request = UpdatePacienteRequest(
                        foto = currentFotoUrl,
                        nombre = nombre,
                        apellidos = apellidos,
                        email = email,
                        telefono = telefono,
                        genero = genero,
                        usuario = usuario,
                        enfermedad = enfermedad,
                        ciudad = ciudad,
                        localidad = localidad,
                        edad = edad,
                        fecha_nacimiento = fechaNacimiento
                    )

                    val response = RetrofitClient.apiService.updatePacienteByEmail(email, request)

                    if (!response.isSuccessful) {
                        val errorBody = response.errorBody()?.string()
                        println("DEBUG - Error en la respuesta (sin foto): $errorBody")
                        return@withContext false
                    }

                    println("DEBUG - Actualización sin foto exitosa")
                    true
                }
            } catch (e: Exception) {
                println("DEBUG - Error general al actualizar paciente: ${e.message}")
                e.printStackTrace()
                false
            }
        }
    }

    private fun createPartFromString(name: String, value: String): RequestBody {
        return value.toRequestBody("text/plain".toMediaTypeOrNull())
    }

    private fun createImagePart(uri: Uri): MultipartBody.Part {
        return try {
            val tempFile = createTempFile(context, uri)
            println("DEBUG - Archivo temporal creado: ${tempFile.absolutePath}")

            val requestFile = tempFile.asRequestBody("image/*".toMediaTypeOrNull())
            MultipartBody.Part.createFormData(
                "foto",
                "profile_${System.currentTimeMillis()}.jpg",
                requestFile
            )
        } catch (e: Exception) {
            println("DEBUG - Error al crear parte de imagen: ${e.message}")
            throw e
        }
    }

    private fun createPacienteRequestBodies(
        nombre: String,
        apellidos: String,
        email: String,
        telefono: String,
        genero: String,
        usuario: String,
        enfermedad: String,
        ciudad: String,
        localidad: String,
        edad: Int,
        fechaNacimiento: String
    ): Map<String, RequestBody> {
        return mapOf(
            "nombre" to nombre.toRequestBody("text/plain".toMediaTypeOrNull()),
            "apellidos" to apellidos.toRequestBody("text/plain".toMediaTypeOrNull()),
            "email" to email.toRequestBody("text/plain".toMediaTypeOrNull()),
            "telefono" to telefono.toRequestBody("text/plain".toMediaTypeOrNull()),
            "genero" to genero.toRequestBody("text/plain".toMediaTypeOrNull()),
            "usuario" to usuario.toRequestBody("text/plain".toMediaTypeOrNull()),
            "enfermedad" to enfermedad.toRequestBody("text/plain".toMediaTypeOrNull()),
            "ciudad" to ciudad.toRequestBody("text/plain".toMediaTypeOrNull()),
            "localidad" to localidad.toRequestBody("text/plain".toMediaTypeOrNull()),
            "edad" to edad.toString().toRequestBody("text/plain".toMediaTypeOrNull()),
            "fecha_nacimiento" to fechaNacimiento.toRequestBody("text/plain".toMediaTypeOrNull())
        )
    }

}