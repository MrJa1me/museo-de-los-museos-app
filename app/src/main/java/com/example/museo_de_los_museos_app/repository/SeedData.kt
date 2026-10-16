package com.example.museo_de_los_museos_app.repository

import com.example.museo_de_los_museos_app.model.CategoriaProducto
import com.example.museo_de_los_museos_app.model.ClasificacionMuseo
import com.example.museo_de_los_museos_app.model.Museum
import com.example.museo_de_los_museos_app.model.Product
import com.example.museo_de_los_museos_app.model.Rol
import com.example.museo_de_los_museos_app.model.User

/**
 * Datos ficticios oficiales del equipo (sección 10 del plan de trabajo).
 * Nadie inventa otros: todos usan estos usuarios, museos y productos.
 */
object SeedData {

    val usuarios: List<User> = listOf(
        User("u01", "Admin Demo", "admin@museo.test", "Admin123", Rol.ADMINISTRADOR),
        User("u02", "Investigador Demo", "investigador@museo.test", "Invest123", Rol.INVESTIGADOR),
        User("u03", "Cliente Uno", "cliente1@museo.test", "Cliente123", Rol.CLIENTE),
        User("u04", "Cliente Dos", "cliente2@museo.test", "Cliente123", Rol.CLIENTE)
    )

    private const val BASE_ENLACE = "https://ejemplo.museo.test/"

    val museos: List<Museum> = listOf(
        Museum(
            id = "m01", nombre = "Museo Virtual de las Culturas Vivas",
            pais = "México", ciudad = "Ciudad de México",
            latitud = 19.4326, longitud = -99.1332,
            clasificacion = ClasificacionMuseo.COMUNIDAD,
            descripcion = "Recorrido digital por las tradiciones y oficios de comunidades mexicanas. Reúne relatos, música y artesanía contados por sus propias voces.",
            enlace = BASE_ENLACE + "m01"
        ),
        Museum(
            id = "m02", nombre = "Museo Virtual del Oro Digital",
            pais = "Colombia", ciudad = "Bogotá",
            latitud = 4.7110, longitud = -74.0721,
            clasificacion = ClasificacionMuseo.HISTORIA,
            descripcion = "Exposición en línea sobre la orfebrería precolombina. Permite explorar piezas en detalle y conocer su contexto histórico.",
            enlace = BASE_ENLACE + "m02"
        ),
        Museum(
            id = "m03", nombre = "Museo Virtual de los Andes",
            pais = "Perú", ciudad = "Lima",
            latitud = -12.0464, longitud = -77.0428,
            clasificacion = ClasificacionMuseo.HISTORIA,
            descripcion = "Viaje virtual por las culturas andinas, sus caminos y su textilería. Incluye mapas interactivos y recorridos guiados.",
            enlace = BASE_ENLACE + "m03"
        ),
        Museum(
            id = "m04", nombre = "Museo Virtual de la Memoria Sur",
            pais = "Chile", ciudad = "Santiago",
            latitud = -33.4489, longitud = -70.6693,
            clasificacion = ClasificacionMuseo.COMUNIDAD,
            descripcion = "Archivo participativo de testimonios y objetos cotidianos del cono sur. Cada persona puede aportar su propia historia.",
            enlace = BASE_ENLACE + "m04"
        ),
        Museum(
            id = "m05", nombre = "Museo Virtual del Río de la Plata",
            pais = "Argentina", ciudad = "Buenos Aires",
            latitud = -34.6037, longitud = -58.3816,
            clasificacion = ClasificacionMuseo.ARTE,
            descripcion = "Galería digital de arte rioplatense contemporáneo. Presenta obras en alta resolución y entrevistas con artistas.",
            enlace = BASE_ENLACE + "m05"
        ),
        Museum(
            id = "m06", nombre = "Museo Virtual de la Selva Urbana",
            pais = "Brasil", ciudad = "São Paulo",
            latitud = -23.5505, longitud = -46.6333,
            clasificacion = ClasificacionMuseo.CIENCIA,
            descripcion = "Muestra interactiva sobre biodiversidad y vida silvestre dentro de la ciudad. Invita a registrar especies observadas.",
            enlace = BASE_ENLACE + "m06"
        ),
        Museum(
            id = "m07", nombre = "Museo Virtual del Candombe",
            pais = "Uruguay", ciudad = "Montevideo",
            latitud = -34.9011, longitud = -56.1645,
            clasificacion = ClasificacionMuseo.ARTE,
            descripcion = "Recorrido sonoro y visual por la música y la danza del candombe. Incluye grabaciones y trajes tradicionales.",
            enlace = BASE_ENLACE + "m07"
        ),
        Museum(
            id = "m08", nombre = "Museo Virtual de la Mitad del Mundo",
            pais = "Ecuador", ciudad = "Quito",
            latitud = -0.1807, longitud = -78.4678,
            clasificacion = ClasificacionMuseo.CIENCIA,
            descripcion = "Espacio de divulgación sobre geografía, astronomía y la línea ecuatorial. Ofrece experimentos virtuales para todas las edades.",
            enlace = BASE_ENLACE + "m08"
        ),
        Museum(
            id = "m09", nombre = "Museo Virtual de las Rutas Atlánticas",
            pais = "España", ciudad = "Madrid",
            latitud = 40.4168, longitud = -3.7038,
            clasificacion = ClasificacionMuseo.HISTORIA,
            descripcion = "Historia de los intercambios culturales a través del Atlántico. Reconstruye rutas, puertos y mercancías en mapas animados.",
            enlace = BASE_ENLACE + "m09"
        ),
        Museum(
            id = "m10", nombre = "Museo Virtual de la Navegación",
            pais = "Portugal", ciudad = "Lisboa",
            latitud = 38.7223, longitud = -9.1393,
            clasificacion = ClasificacionMuseo.HISTORIA,
            descripcion = "Colección digital de instrumentos y cartas náuticas de la era de los descubrimientos. Permite recorrer cada pieza en 3D.",
            enlace = BASE_ENLACE + "m10"
        )
    )

    /** Cursos: el stock son cupos y no tienen peso (sin envío). El ítem a02 está agotado a propósito. */
    val productos: List<Product> = listOf(
        Product("c01", "Introducción a la Museología", CategoriaProducto.CURSO, 45000, 30,
            descripcion = "Curso introductorio a los conceptos y prácticas de la museología."),
        Product("c02", "Mediación Cultural en Museos", CategoriaProducto.CURSO, 52000, 25,
            descripcion = "Estrategias para acercar las colecciones a distintos públicos."),
        Product("c03", "Gestión de Audiencias", CategoriaProducto.CURSO, 58000, 20,
            descripcion = "Herramientas para conocer, atraer y fidelizar audiencias."),
        Product("c04", "Conservación Preventiva", CategoriaProducto.CURSO, 60000, 15,
            descripcion = "Buenas prácticas para cuidar colecciones y prevenir su deterioro."),
        Product("p01", "Guía de Museos Virtuales (libro)", CategoriaProducto.PUBLICACION, 12000, 25, 0.5,
            "Guía impresa con una selección de museos virtuales de Iberoamérica."),
        Product("p02", "Cuaderno de Mediación (libro)", CategoriaProducto.PUBLICACION, 9000, 40, 0.4,
            "Cuaderno de actividades para planificar experiencias de mediación."),
        Product("m01k", "Kit de guantes y papel libre de ácido", CategoriaProducto.MATERIAL_CONSERVACION, 18000, 30, 0.8,
            "Kit básico para manipular y proteger documentos y objetos."),
        Product("m02k", "Caja de conservación archivística", CategoriaProducto.MATERIAL_CONSERVACION, 24000, 15, 1.5,
            "Caja libre de ácido para guardar documentos de forma segura."),
        Product("a01", "Bolso de tela Museo de los Museos", CategoriaProducto.ARTICULO_MUSEOLOGICO, 8000, 50, 0.2,
            "Bolso de tela reutilizable con el logo del museo."),
        Product("a02", "Taza Museo de los Museos", CategoriaProducto.ARTICULO_MUSEOLOGICO, 7000, 0, 0.4,
            "Taza de cerámica con el logo del museo. Agotada.")
    )
}
