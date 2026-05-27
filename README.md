# 📱 MedicitaApp

## 📌 Descripción del Proyecto

MedicitaApp es una aplicación móvil desarrollada en Android utilizando Jetpack Compose. Permite a los usuarios gestionar la solicitud de medicamentos mediante el envío de fórmulas médicas digitales (imagen o PDF) y consultar el estado de sus solicitudes.

Además, incluye un módulo para el farmacéuta, quien puede revisar, aprobar, rechazar o aplazar solicitudes, así como asignar turnos digitales y gestionar medicamentos.

---

## 👥 Integrantes

- Camilo Andres Mora Garzon
- Luis Eduardo Fino

---

## ⚙️ Tecnologías utilizadas

| Tecnología        | Uso                                   |
| ----------------- | ------------------------------------- |
| **Lenguaje**      | Kotlin                                |
| **UI**            | Jetpack Compose                       |
| **Arquitectura**  | MVVM                                  |
| **Navegación**    | Navigation Compose (NavHost)          |
| **Base de datos** | Room Database                         |
| **Persistencia**  | Local (Room + SessionManager)         |
| **Networking**    | Retrofit + Gson                       |
| **API**           | Gemini AI para validación de fórmulas |
| **IDE**           | Android Studio                        |

---

## 📱 Funcionalidades principales

### 👤 Usuario

- Registro e inicio de sesión
- Recuperación de contraseña
- Subida de fórmulas médicas (cámara, galería o PDF)
- Validación automática con IA (Gemini)
- Consulta del estado de solicitudes
- Visualización de turno digital (con tiempo de espera y posición)
- Recepción de notificaciones push
- Perfil de usuario con historial de solicitudes
- Persistencia de sesión (no necesita volver a iniciar sesión)

### 💊 Farmacéuta

- Inicio de sesión especial (admin/1234)
- Visualización de solicitudes por estado (Pendientes, Aceptadas, Rechazadas, Aplazadas)
- Revisión de fórmulas médicas (ver imagen/PDF)
- Acciones sobre solicitudes:
  - ✅ Aprobar (con turno automático)
  - ❌ Rechazar
  - ⏰ Aplazar
  - 🎉 Marcar como listo
- Asignación de turno digital con:
  - Número de turno (ej: A-42)
  - Ubicación de la farmacia
  - Tiempo estimado de espera
  - Posición en la cola
- Gestión de medicamentos (CRUD con API de datos.gov.co)

---

## 🗂️ Estructura del Proyecto

app/
├── src/main/java/com/example/medicitaapp/
│ ├── admin/ # Pantallas del farmacéuta
│ ├── data/ # Room, DAOs, Entities, SessionManager
│ ├── navigation/ # AppNavHost, AppRoutes
│ ├── services/ # GeminiService, NotificationService
│ ├── ui/ # Componentes reutilizables
│ ├── user/ # Pantallas de usuario
│ ├── viewmodel/ # AuthViewModel
│ └── MainActivity.kt # Punto de entrada
├── res/
│ ├── drawable/ # Iconos y recursos gráficos
│ ├── xml/ # file_paths.xml para FileProvider
│ └── ...
└── build.gradle.kts # Dependencias

---

## 🔄 Navegación

La aplicación implementa navegación usando `NavHost`, permitiendo flujo entre:

- **Login** → Registro / Home / Farmacéuta / Recuperación
- **Registro** → Login
- **Home** → Subir fórmula / Turno / Notificaciones / Perfil
- **Farmacéuta** → Solicitudes / Gestión de medicamentos

**Persistencia de sesión:** La app recuerda si el usuario o farmacéuta estaba logueado, incluso después de cerrar la app.

---

## 💾 Persistencia de datos

Se utiliza Room Database para almacenar:

- **Usuarios** (nombre, documento, teléfono, contraseña)
- **Solicitudes de fórmulas** (URI, tipo, estado, turno, ubicación)
- **Notificaciones** (título, mensaje, estado de lectura)
- **Medicamentos** (catálogo desde API)

Los datos persisten entre ejecuciones de la aplicación.

---

## 🔐 API Key (Gemini)

La API Key de Gemini se maneja de forma segura:

- Se almacena en `local.properties` (NO subir a GitHub)
- Se inyecta en tiempo de compilación con `BuildConfig`
- El repositorio público NO contiene la API key

**Configuración local:**

```properties
# local.properties
GEMINI_API_KEY=tu_api_key_aqui
```
