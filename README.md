# NutriFit

**NutriFit Planner**:  Plataforma integral para planificación de citas y planes nutricionales.


---

## ¡Actualización!

Ahora ya no necesitas tener corriendo nuestro sistema NutriFit - Panel Administrativo en su equipo, ahora contamos con conexión a servidores remotos!

Solamente Descarga la APK: 

[![Descargar APK](https://img.shields.io/badge/Descargar-APK-green?style=for-the-badge&logo=android)](https://drive.google.com/uc?export=download&id=1iAuAPX0vHnKGTtTjePfSMj-6fITkEI3r)

👉 [https://nutrifitplanner.site/](https://nutrifitplanner.site/)

Y automaticamente se conectara a nuestros servidores de ultima tecnologia en ASCRIB.

### Si no deseas utilizar el apk y quieres probar de forma local, entonces prosigue con los pasos de guía.

---

### Previsualización NutriFit Planner 

## Inicio

<img src="https://drive.google.com/uc?export=view&id=1LJFfq52c8IwTsMhK_fTl5ieOtHOl8QBB" alt="Inicio" width="250" height="500"/>

<img src="https://drive.google.com/uc?export=view&id=10EXCsis4B8kR2S8p5bv74u8qIVhSsOxw" alt="Inicio" width="250" height="500"/>

<img src="https://drive.google.com/uc?export=view&id=1Cro56OB5_8qEbEsVxzYSi1fblR893NBX" alt="Inicio" width="250" height="500"/>


## Panel  

<img src="https://drive.google.com/uc?export=view&id=11eaHlPcYmGBO8fWBEjG04_jGYKxmOIed" alt="Inicio" width="250" height="500"/>

<img src="https://drive.google.com/uc?export=view&id=1kj8lFOgRMBn0izPtpC5BzZh2USMr-QPd" alt="Inicio" width="250" height="500"/>

<img src="https://drive.google.com/uc?export=view&id=1OurRpyHbIcFF0eAH4QTwvf4jI0ALa1O6" alt="Inicio" width="250" height="500"/>

<img src="https://drive.google.com/uc?export=view&id=12sA6Bg895subH4j2GQVgqgMHaA71Ah91" alt="Inicio" width="250" height="500"/>

<img src="https://drive.google.com/uc?export=view&id=17Gm-JEb79gB3W7z0JO18unsl-bC970ma" alt="Inicio" width="250" height="500"/>


---


## 📖 Descripción

NutriFit es una plataforma para la planificación de citas y la creación de planes nutricionales personalizados. La aplicación permite a los usuarios llevar un seguimiento detallado de sus citas y objetivos alimenticios, con herramientas para planificar y ajustar sus dietas según sus necesidades.

---

## Características

- 📅 Planificación de citas nutricionales.
- 🍎 Creación y seguimiento de planes alimenticios personalizados.
- 👤 Interfaz intuitiva para usuarios y profesionales de la salud.
- 📊 Estadísticas detalladas sobre el progreso nutricional.

---

## 🛠️ Requisitos

Antes de ejecutar el proyecto, asegúrate de tener las siguientes herramientas y versiones instaladas:

- **Android Studio**: [Descargar Android Studio](https://developer.android.com/studio)
- **Gradle 8.13**: Asegúrate de usar la versión correcta de Gradle para evitar problemas al compilar el proyecto.

    Para configurar Gradle 8.13, sigue estos pasos:

    1. **Descargar Gradle**: Ve a [gradle.org/releases](https://gradle.org/releases/) y descarga la versión 8.13.
    2. **Configurar Gradle en Android Studio**:
        - Abre **Android Studio**.
        - Ve a **File > Settings > Build, Execution, Deployment > Build Tools > Gradle**.
        - Selecciona **Use default gradle wrapper (recommended)** y verifica que la versión sea **8.13**.

---

## Instalación

Sigue estos pasos para clonar y ejecutar el proyecto localmente:

1. Clona este repositorio:

    ```bash
    git clone https://github.com/tu_usuario/NutriFit.git
    ```

2. Navega al directorio del proyecto:

    ```bash
    cd NutriFit
    ```

3. Asegúrate de tener **Gradle 8.13** configurado en Android Studio.

4. Abre el proyecto en Android Studio y selecciona **Build > Make Project** para compilarlo.

5. En caso que se le presente inconveninetes, por favor de clonar unicamente la rama AlanPuc_Pruebas

---

## ⚙️ Configuración de conexión local a la base de datos

Como este proyecto está en fase de prototipo, la conexión al servidor es local. Asegúrate de que el dispositivo móvil y la computadora que ejecuta el servidor web (NutriFit - Panel Administrativo) estén conectados a la misma red Wi-Fi.

### 🔧 Archivos a modificar
Ubícate en la carpeta com.ascrib.nutrifit, y realiza los siguientes cambios:

  ### 1. api/RetrofitClient.kt . Busca la línea:
    
   ```bash
       private const val BASE_URL = "http://192.168.50.221:8000/"
 ```
🔁 Reemplaza 192.168.50.221 por tu dirección IPv4 local. Puedes obtenerla con:

   ```bash
       ipconfig   # En Windows
 ```

### 2. ui/dashboard/adapter/ReservacionesAdapter.kt
Busca:

```bash
    val correctedUrl = fotoUrl.replace("http://127.0.0.1:8000", "http://192.168.50.221:8000")
```
🔁 Sustituye 192.168.50.221 por tu IPv4.

### 3. ui/form/planList/PlanListFragment.kt
Busca:
   
```bash
    val correctedUrl = url.replace("127.0.0.1", "192.168.50.221")
```
🔁 Sustituye también aquí la dirección "192.168.50.221" por tu IPv4.

### 4. res/xml/network_security_config.xml
Agrega tu IPv4 a la lista de dominios permitidos para evitar errores de conexión por seguridad de Android.
   Ejemplo:

```bash
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">192.168.50.1</domain> <!-- Ejemplo existente -->
        <domain includeSubdomains="true">TU_DIRECCION_IPV4</domain> <!-- Agrega esta línea -->
    </domain-config>
</network-security-config>
```
⚠️ Nota: Sustituye TU_DIRECCION_IPV4 por la IP obtenida con ipconfig.

---

## 📱 Ejecución

- 1 Conecta tu celular o inicia un emulador.
- 2 Asegúrate de estar en la misma red Wi-Fi que el backend.
- 3 Haz clic en Run (ícono ▶️) en Android Studio.

---

## Contribuciones

¡Las contribuciones son bienvenidas! Si tienes alguna idea para mejorar el proyecto, por favor abre un **pull request** o un **issue**.

---

## Contacto

Para más información, no dudes en ponerte en contacto:

- **Correo electrónico**: [puc-alan20@hotmail.com](puc-alan20@hotmail.com)
- **GitHub**: [@WoolyOlvel](https://github.com/WoolyOlvel)
  
---

## 📄 Licencia
Este software se proporciona únicamente con fines de visualización. No está permitido modificar, redistribuir ni reutilizar el código sin autorización expresa del autor.

---

## © Derechos de Autor
NutriFit Planner es un producto desarrollado y propiedad intelectual de:
### ASCRIB
  - Fundador: Alan Puc Yam
  - Todos los derechos reservados.
