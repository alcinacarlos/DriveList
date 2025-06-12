# DriveList: Mercado de Coches de Segunda Mano

![Logo de la Aplicación](./app/src/main/res/drawable/ic_logo.png)

**DriveList** es una aplicación móvil Android nativa, diseñada para ser el punto de encuentro entre compradores y vendedores de coches de segunda mano. Construida con Kotlin y Jetpack Compose y siguiendo las mejores prácticas de arquitectura de software, ofreciendo una experiencia de usuario fluida, segura y con muchas funcionalidades

---

## Funcionalidades Principales

* **Gestión de Usuarios Completa:** Sistema de registro e inicio de sesión con **Firebase Authentication** (email/contraseña, Google). Incluye funcionalidades de seguridad como el restablecimiento de contraseña
* **Publicación y Edición de Anuncios:** Un flujo de UI intuitivo permite a los usuarios publicar y editar sus anuncios de coches con información detallada, múltiples imágenes y localización
* **Búsqueda Inteligente y Filtros:** Una pantalla de búsqueda dedicada con una barra de texto, filtros rápidos (ej. "Diésel", "< 10.000€") y un panel de filtros avanzados (marca, modelo, año, precio, ubicación)
* **Geolocalización:** Detección automática de la ubicación del vehículo mediante GPS y validación de códigos postales a través de la API de **GeoNames**.
* **Sistema de Favoritos:** Los usuarios pueden marcar coches como favoritos para un acceso rápido, con un estado que se sincroniza en tiempo real
* **Personalización de la App:** Ajustes para cambiar el **Idioma**, el **Tema** (Claro, Oscuro, Sistema) con soporte para **Colores Dinámicos (Material You)**, y la **Tipografía** de la aplicación
* **Chat:** Funcionalidad para iniciar un chat directo entre comprador y vendedor en tiempo real

---

## Arquitectura del Proyecto

La aplicación está construida siguiendo los principios de **Clean Architecture**, promoviendo un código desacoplado, escalable, independiente del framework y altamente testeable. La arquitectura se divide en tres capas principales con una estricta regla de dependencias: la capa de UI depende del Dominio, y la capa de Datos depende del Dominio. La capa de Dominio es completamente independiente.

**UI Layer  ➔  Domain Layer  ➔  Data Layer**

### 1. Capa de Presentación (UI)
Esta capa es responsable de todo lo relacionado con la interfaz de usuario. Sigue el patrón **MVVM (Model-View-ViewModel)** recomendado por Google.

* **Vistas (Compose Screens):** Construidas 100% con **Jetpack Compose** y **Material 3**. Son responsables de dibujar el estado recibido del ViewModel y de notificarle las interacciones del usuario. No contienen lógica de negocio.
* **ViewModels:** Actúan como intermediarios entre las Vistas y la lógica de negocio. Exponen el estado de la UI a través de `StateFlow` y reciben los eventos de la UI. Orquestan las acciones llamando a los Reposorios (o Casos de Uso). Se utiliza **Hilt** para la inyección de dependencias.
* **UI State:** `data class` de Kotlin que modelan el estado completo de cada pantalla de forma inmutable, facilitando un flujo de datos unidireccional (UDF).

### 2. Capa de Dominio (Domain)
Es el corazón de la aplicación. Contiene la lógica de negocio y las reglas fundamentales, y es completamente independiente de cualquier detalle de implementación de la UI o de la base de datos.

* **Modelos de Dominio:** Clases de Kotlin (`data class`, `enum class`) que representan las entidades centrales del negocio (ej. `CarForSale`, `UserDisplayInfo`, `CarColor`). Son agnósticas a la base de datos o a la API.
* **Interfaces de Repositorio:** Definen los "contratos" que la capa de datos debe cumplir. Abstraen el origen de los datos (ej. `CarListRepository`, `UserFavoriteRepository`). Esto permite cambiar la fuente de datos (ej. de Firestore a una API REST) sin afectar la capa de dominio o de UI.


### 3. Capa de Datos (Data)
Esta capa es responsable de la implementación concreta de la obtención y almacenamiento de los datos.

* **Implementaciones de Repositorio:** Clases que implementan las interfaces del dominio (ej. `CarListRepositoryImpl`). Contienen la lógica para decidir de qué fuente de datos obtener la información (remota o local).
* **Fuentes de Datos (Data Sources):** Clases que interactúan directamente con una fuente de datos específica:
    * **Remotas:** Lógica para interactuar con **Firebase Firestore**, **Firebase Storage**, **Firebase Authentication**, y la API de **GeoNames** (usando **Retrofit**).
    * **Locales:** Lógica para interactuar con la persistencia local, como **Jetpack DataStore** para guardar las preferencias del usuario (tema, idioma).
* **DTOs (Data Transfer Objects):** Modelos de datos específicos para la respuesta de APIs externas (ej. `GeoNamesResponse`). Se mapean a los modelos de dominio antes de pasar la información a las capas superiores.

---

## Stack Tecnológico y Librerías

* **Lenguaje:** **Kotlin** 100%
* **Interfaz de Usuario (UI):**
    * **Jetpack Compose:** Toolkit declarativo moderno para la construcción de la UI.
    * **Material 3:** Implementación de las guías de diseño de Google, con soporte para **Colores Dinámicos (Material You)**.
    * **Navigation Compose:** Para la navegación entre pantallas (Composables).
    * **Coil:** Para la carga de imágenes asíncrona y eficiente.
* **Arquitectura:**
    * **MVVM (Model-View-ViewModel)**
    * **Clean Architecture** (Capas de UI, Dominio y Datos)
    * **Flujo de Datos Unidireccional (UDF)** con `StateFlow` y `SharedFlow`.
* **Inyección de Dependencias:**
    * **Hilt:** Para la inyección de dependencias en toda la aplicación.
* **Programación Asíncrona:**
    * **Kotlin Coroutines & Flow:** Para manejar operaciones en segundo plano y flujos de datos reactivos.
* **Backend y Base de Datos (Firebase):**
    * **Firebase Authentication:** Para la gestión de usuarios (Email/Password, Google).
    * **Firebase Firestore:** Como base de datos NoSQL principal para anuncios, favoritos, etc.
    * **Firebase Storage:** Para el almacenamiento de imágenes.
* **Red (Networking):**
    * **Retrofit:** Para las llamadas a la API REST de GeoNames.
    * **Gson:** Para la conversión de JSON a objetos Kotlin.
    * **OkHttp Logging Interceptor:** Para depurar las llamadas de red.
* **Persistencia Local:**
    * **Jetpack DataStore Preferences:** Para guardar ajustes del usuario (tema, idioma) de forma moderna y segura.
* **Localización:**
    * **Google Play Services Location:** Para obtener la ubicación del dispositivo con `FusedLocationProviderClient`.
    * **GeoNames API:** Servicio web externo para la validación de códigos postales.

---

## Configuración del Proyecto

Para ejecutar este proyecto, necesitarás configurar tu entorno de Firebase y obtener las credenciales necesarias.

1.  **Configuración de Firebase:**
    * Crea un nuevo proyecto en la [Consola de Firebase](https://console.firebase.google.com/).
    * Añade una aplicación Android. Sigue los pasos para descargar el archivo `google-services.json` y colócalo en el directorio `app/` de tu proyecto.
    * Habilita los siguientes servicios: **Authentication** (Email/Password, Google), **Firestore**, y **Storage**.
    * Configura las **Reglas de Seguridad** para permitir el acceso a los usuarios autenticados.

2.  **APIs Externas:**
    * **GeoNames:** Regístrate en [geonames.org](http://www.geonames.org/export/web-services.html) para obtener un nombre de usuario. Guárdalo en tu archivo `local.properties`: `geonames.username="TU_USERNAME"`.
    * **MeiliSearch:** Si usas MeiliSearch, necesitarás la URL de tu instancia y la clave de API. Guárdalas de forma segura, preferiblemente en `local.properties`.

3.  **Fuentes:**
    * Añade los archivos de fuentes (`.ttf` o `.otf`) para las familias "Poppins" y "Urbanist" en la carpeta `app/src/main/res/font/`.

---

## 📂 Estructura del Proyecto

El proyecto sigue una organización modular basada en Clean Architecture:

```plaintext
com.carlosalcina.drivelist/
├── data/
│   ├── datasource/         # Fuentes de datos remotas (Firestore, Storage)
│   ├── location/           # Lógica de geolocalización
│   ├── preferences/        # Repositorios para DataStore (Tema, Idioma)
│   ├── remote/             # DTOs y servicios API (Retrofit para GeoNames)
│   └── repository/         # Implementaciones de las interfaces de Repositorio
├── di/                     # Módulos de Hilt para inyección de dependencias
├── domain/
│   ├── model/              # Entidades y modelos de negocio (CarForSale, UserDetails)
│   ├── repository/         # Interfaces de Repositorio (abstracciones)
│   └── usecase/            # Casos de uso con lógica de negocio específica
├── localization/           # Clases relacionadas con el idioma de la app
├── navigation/             # Definiciones de rutas y grafos de navegación
├── ui/
│   ├── components/         # Componentes de UI reutilizables (BottomBar, etc.)
│   ├── states/             # Data classes que representan el estado de la UI
│   ├── theme/              # Definiciones de Color, Tipografía y Tema de la app
│   ├── view/
│   │   └── screens/        # Composables que representan cada pantalla completa
│   └── viewmodel/          # ViewModels para cada pantalla
└── utils/                  # Clases de utilidad (KeywordGenerator, NetworkUtils, etc.)
```

