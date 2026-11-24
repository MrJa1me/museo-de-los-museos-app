package com.museo.museos.service;

import com.museo.museos.dto.MuseoRequest;
import com.museo.museos.exception.ConflictoException;
import com.museo.museos.exception.RecursoNoEncontradoException;
import com.museo.museos.model.Museo;
import com.museo.museos.repository.MuseoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Lógica de negocio de las fichas. El controlador solo recibe y responde; las reglas viven aquí. */
@Service
public class MuseoService {

    private static final Pattern ID_MUSEO = Pattern.compile("^m(\\d+)$");
    private static final String PROYECTO_POR_DEFECTO = "Mapa Iberoamérica 2026";

    private final MuseoRepository repositorio;

    public MuseoService(MuseoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** publicado = null devuelve todas; true solo las aprobadas (lo que ve un Cliente); false las pendientes. */
    public List<Museo> listar(Boolean publicado) {
        if (publicado == null) return repositorio.findAllByOrderByNombreAsc();
        return repositorio.findByPublicadoOrderByNombreAsc(publicado);
    }

    public Museo obtener(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el museo " + id));
    }

    /** Una ficha nueva siempre queda pendiente: solo el Administrador la publica con aprobar(). */
    @Transactional
    public Museo crear(MuseoRequest datos) {
        String id = (datos.id() == null || datos.id().isBlank()) ? siguienteId() : datos.id();
        if (repositorio.existsById(id)) {
            throw new ConflictoException("Ya existe un museo con el id " + id);
        }
        Museo museo = new Museo();
        museo.setId(id);
        copiar(datos, museo);
        museo.setPublicado(false);
        return repositorio.save(museo);
    }

    /** Editar no cambia el estado de publicación. */
    @Transactional
    public Museo actualizar(String id, MuseoRequest datos) {
        Museo museo = obtener(id);
        copiar(datos, museo);
        return repositorio.save(museo);
    }

    @Transactional
    public Museo aprobar(String id) {
        Museo museo = obtener(id);
        museo.setPublicado(true);
        return repositorio.save(museo);
    }

    @Transactional
    public void eliminar(String id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el museo " + id);
        }
        repositorio.deleteById(id);
    }

    /** Siguiente id libre: m01…m10 → "m11". Misma regla que ValidadorFichaMuseo.siguienteId en la app. */
    String siguienteId() {
        int mayor = 0;
        for (Museo museo : repositorio.findAll()) {
            Matcher m = ID_MUSEO.matcher(museo.getId());
            if (m.matches()) mayor = Math.max(mayor, Integer.parseInt(m.group(1)));
        }
        return String.format("m%02d", mayor + 1);
    }

    private void copiar(MuseoRequest datos, Museo museo) {
        museo.setNombre(datos.nombre().trim());
        museo.setPais(datos.pais().trim());
        museo.setCiudad(datos.ciudad().trim());
        museo.setLatitud(datos.latitud());
        museo.setLongitud(datos.longitud());
        museo.setClasificacion(datos.clasificacion());
        museo.setDescripcion(datos.descripcion().trim());
        museo.setEnlace(datos.enlace().trim());
        if (datos.proyecto() != null && !datos.proyecto().isBlank()) {
            museo.setProyecto(datos.proyecto().trim());
        } else if (museo.getProyecto() == null) {
            museo.setProyecto(PROYECTO_POR_DEFECTO);
        }
        if (datos.imagen() != null) museo.setImagen(datos.imagen().trim());
    }
}
