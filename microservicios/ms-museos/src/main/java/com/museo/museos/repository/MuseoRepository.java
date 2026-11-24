package com.museo.museos.repository;

import com.museo.museos.model.Museo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data JPA genera el SQL a partir del nombre de cada método. */
public interface MuseoRepository extends JpaRepository<Museo, String> {

    List<Museo> findByPublicadoOrderByNombreAsc(boolean publicado);

    List<Museo> findAllByOrderByNombreAsc();
}
