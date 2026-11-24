package com.museo.museos.config;

import com.museo.museos.model.Clasificacion;
import com.museo.museos.model.Museo;
import com.museo.museos.repository.MuseoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Carga los 10 museos ficticios del equipo (los mismos de SeedData.kt en la app)
 * solo la primera vez, cuando la tabla está vacía.
 */
@Component
public class DatosIniciales implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DatosIniciales.class);
    private static final String BASE_ENLACE = "https://ejemplo.museo.test/";

    private final MuseoRepository repositorio;

    public DatosIniciales(MuseoRepository repositorio) {
        this.repositorio = repositorio;
    }

    @Override
    public void run(String... args) {
        if (repositorio.count() > 0) return;
        repositorio.saveAll(museosSemilla());
        log.info("Se cargaron {} museos de prueba", repositorio.count());
    }

    static List<Museo> museosSemilla() {
        return List.of(
                new Museo("m01", "Museo Virtual de las Culturas Vivas", "México", "Ciudad de México", 19.4326, -99.1332,
                        Clasificacion.COMUNIDAD,
                        "Recorrido digital por las tradiciones y oficios de comunidades mexicanas. Reúne relatos, música y artesanía contados por sus propias voces.",
                        BASE_ENLACE + "m01", "museo_culturas_vivas", true),
                new Museo("m02", "Museo Virtual del Oro Digital", "Colombia", "Bogotá", 4.7110, -74.0721,
                        Clasificacion.HISTORIA,
                        "Exposición en línea sobre la orfebrería precolombina. Permite explorar piezas en detalle y conocer su contexto histórico.",
                        BASE_ENLACE + "m02", "museo_oro", true),
                new Museo("m03", "Museo Virtual de los Andes", "Perú", "Lima", -12.0464, -77.0428,
                        Clasificacion.HISTORIA,
                        "Viaje virtual por las culturas andinas, sus caminos y su textilería. Incluye mapas interactivos y recorridos guiados.",
                        BASE_ENLACE + "m03", "museo_de_los_andes", true),
                new Museo("m04", "Museo Virtual de la Memoria Sur", "Chile", "Santiago", -33.4489, -70.6693,
                        Clasificacion.COMUNIDAD,
                        "Archivo participativo de testimonios y objetos cotidianos del cono sur. Cada persona puede aportar su propia historia.",
                        BASE_ENLACE + "m04", "museo_de_la_memoria", true),
                new Museo("m05", "Museo Virtual del Río de la Plata", "Argentina", "Buenos Aires", -34.6037, -58.3816,
                        Clasificacion.ARTE,
                        "Galería digital de arte rioplatense contemporáneo. Presenta obras en alta resolución y entrevistas con artistas.",
                        BASE_ENLACE + "m05", "museo_rio_de_la_plata", true),
                new Museo("m06", "Museo Virtual de la Selva Urbana", "Brasil", "São Paulo", -23.5505, -46.6333,
                        Clasificacion.CIENCIA,
                        "Muestra interactiva sobre biodiversidad y vida silvestre dentro de la ciudad. Invita a registrar especies observadas.",
                        BASE_ENLACE + "m06", "museo_de_la_selva_urbana", true),
                new Museo("m07", "Museo Virtual del Candombe", "Uruguay", "Montevideo", -34.9011, -56.1645,
                        Clasificacion.ARTE,
                        "Recorrido sonoro y visual por la música y la danza del candombe. Incluye grabaciones y trajes tradicionales.",
                        BASE_ENLACE + "m07", "museo_del_candombe", true),
                new Museo("m08", "Museo Virtual de la Mitad del Mundo", "Ecuador", "Quito", -0.1807, -78.4678,
                        Clasificacion.CIENCIA,
                        "Espacio de divulgación sobre geografía, astronomía y la línea ecuatorial. Ofrece experimentos virtuales para todas las edades.",
                        BASE_ENLACE + "m08", "museo_mitad_del_mundo", true),
                new Museo("m09", "Museo Virtual de las Rutas Atlánticas", "España", "Madrid", 40.4168, -3.7038,
                        Clasificacion.HISTORIA,
                        "Historia de los intercambios culturales a través del Atlántico. Reconstruye rutas, puertos y mercancías en mapas animados.",
                        BASE_ENLACE + "m09", "museo_rutas_atlanticas", true),
                new Museo("m10", "Museo Virtual de la Navegación", "Portugal", "Lisboa", 38.7223, -9.1393,
                        Clasificacion.HISTORIA,
                        "Colección digital de instrumentos y cartas náuticas de la era de los descubrimientos. Permite recorrer cada pieza en 3D.",
                        BASE_ENLACE + "m10", "museo_de_la_navegacion", true)
        );
    }
}
