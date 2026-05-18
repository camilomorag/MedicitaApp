SUBO EL REDME POR ACA Y NO AL GITHUB DEBIDO A COMPLICACIONES TECNICAS PERO PARA AL OTRA ENTREGA SE SUBIRA TOALMENTE A EL GIT

# 📱 MedicitaApp

## 📌 Descripción del Proyecto

MedicitaApp es una aplicación móvil desarrollada en Android utilizando Jetpack Compose. Permite a los usuarios gestionar la solicitud de medicamentos mediante el envío de fórmulas médicas digitales y consultar el estado de sus solicitudes.

Además, incluye un módulo para el farmacéuta, quien puede revisar, aprobar, rechazar o aplazar solicitudes.

---

## 👥 Integrantes

Camilo Andres Mora Garzon
Luis Eduardo Fino

---

## ⚙️ Tecnologías utilizadas

- **Lenguaje:** Kotlin
- **UI:** Jetpack Compose
- **Arquitectura:** MVVM
- **Navegación:** Navigation Compose (NavHost)
- **Base de datos:** Room Database
- **Persistencia:** Local (Room + SessionManager)
- **IDE:** Android Studio

---

## 📱 Funcionalidades principales

### 👤 Usuario

- Registro e inicio de sesión
- Subida de fórmulas médicas (imagen o archivo)
- Consulta del estado de solicitudes
- Visualización de turno digital
- Recepción de notificaciones
- Perfil de usuario con historial

### 💊 Farmacéuta

- Inicio de sesión especial
- Visualización de solicitudes
- Revisión de fórmulas médicas
- Acciones:
  - Aprobar
  - Rechazar
  - Aplazar
  - Marcar como listo

- Asignación de turno y ubicación

---

## 🔄 Navegación

La aplicación implementa navegación usando `NavHost`, permitiendo flujo entre:

- Login
- Registro
- Home
- Subida de fórmula
- Estado de turno
- Notificaciones
- Perfil
- Panel farmacéuta

---

## 💾 Persistencia de datos

Se utiliza Room Database para almacenar:

- Usuarios
- Solicitudes de fórmulas
- Notificaciones

Los datos persisten entre ejecuciones de la aplicación.

---

## ▶️ Cómo ejecutar el proyecto

1. Clonar el repositorio:

```bash
git clone [URL_DEL_REPOSITORIO]
```

2. Abrir en Android Studio

3. Ejecutar en un emulador o dispositivo físico

---

## ✅ Estado del proyecto

✔ Interfaz de usuario completa
✔ Navegación funcional
✔ Persistencia con Room
✔ Flujo usuario-farmacéuta implementado

---

## 📌 Notas

- El sistema de carga de archivos funciona con almacenamiento local.
- Se implementara para la otra entrega los mediacamentos con el room

---
