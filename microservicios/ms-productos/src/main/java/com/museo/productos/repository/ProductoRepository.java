package com.museo.productos.repository;

import com.museo.productos.model.Categoria;
import com.museo.productos.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Spring Data JPA genera el SQL a partir del nombre de cada método. */
public interface ProductoRepository extends JpaRepository<Producto, String> {

    List<Producto> findAllByOrderByIdAsc();

    List<Producto> findByActivoOrderByIdAsc(boolean activo);

    List<Producto> findByCategoriaOrderByIdAsc(Categoria categoria);

    List<Producto> findByCategoriaAndActivoOrderByIdAsc(Categoria categoria, boolean activo);
}
