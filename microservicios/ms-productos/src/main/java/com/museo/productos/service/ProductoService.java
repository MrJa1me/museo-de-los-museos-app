package com.museo.productos.service;

import com.museo.productos.dto.DisponibilidadResponse;
import com.museo.productos.dto.ItemStockRequest;
import com.museo.productos.dto.MotivoProblema;
import com.museo.productos.dto.ProblemaStock;
import com.museo.productos.dto.ProductoRequest;
import com.museo.productos.exception.ConflictoException;
import com.museo.productos.exception.DatoInvalidoException;
import com.museo.productos.exception.RecursoNoEncontradoException;
import com.museo.productos.model.Categoria;
import com.museo.productos.model.Producto;
import com.museo.productos.repository.ProductoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lógica de negocio del catálogo. El controlador solo recibe y responde; las reglas viven aquí:
 *  - el servidor asigna el id según la categoría (c05, p03, m03k, a03);
 *  - los cursos no tienen peso y el resto sí;
 *  - editar no cambia si el producto está activo (eso se hace con cambiarActivo);
 *  - antes de confirmar una compra se revisa la disponibilidad y luego se descuenta el stock.
 */
@Service
public class ProductoService {

    private final ProductoRepository repositorio;

    public ProductoService(ProductoRepository repositorio) {
        this.repositorio = repositorio;
    }

    /** Filtros opcionales: ?categoria=CURSO y/o ?activo=true (lo que ve un Cliente). */
    public List<Producto> listar(Categoria categoria, Boolean activo) {
        if (categoria != null && activo != null) return repositorio.findByCategoriaAndActivoOrderByIdAsc(categoria, activo);
        if (categoria != null) return repositorio.findByCategoriaOrderByIdAsc(categoria);
        if (activo != null) return repositorio.findByActivoOrderByIdAsc(activo);
        return repositorio.findAllByOrderByIdAsc();
    }

    public Producto obtener(String id) {
        return repositorio.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el producto " + id));
    }

    /** Alta del Administrador. Un producto nuevo siempre parte activo. */
    @Transactional
    public Producto crear(ProductoRequest datos) {
        validarPeso(datos);
        String id = (datos.id() == null || datos.id().isBlank()) ? siguienteId(datos.categoria()) : datos.id();
        if (repositorio.existsById(id)) {
            throw new ConflictoException("Ya existe un producto con el id " + id);
        }
        Producto producto = new Producto();
        producto.setId(id);
        copiar(datos, producto);
        producto.setActivo(true);
        return repositorio.save(producto);
    }

    /** Edición del Administrador: conserva el id y si está activo. */
    @Transactional
    public Producto actualizar(String id, ProductoRequest datos) {
        validarPeso(datos);
        Producto producto = obtener(id);
        copiar(datos, producto);
        return repositorio.save(producto);
    }

    /** Desactivar no borra: el producto deja de verse en el catálogo del Cliente, pero los pedidos antiguos lo siguen nombrando. */
    @Transactional
    public Producto cambiarActivo(String id, boolean activo) {
        Producto producto = obtener(id);
        producto.setActivo(activo);
        return repositorio.save(producto);
    }

    @Transactional
    public void eliminar(String id) {
        if (!repositorio.existsById(id)) {
            throw new RecursoNoEncontradoException("No existe el producto " + id);
        }
        repositorio.deleteById(id);
    }

    /**
     * Revisa, sin cambiar nada, si cada línea del carrito se puede comprar.
     * La app la llama antes de crear la solicitud en ms-pedidos (el stock real está aquí, no en el teléfono).
     */
    public DisponibilidadResponse verificarDisponibilidad(List<ItemStockRequest> items) {
        List<ProblemaStock> problemas = new ArrayList<>();
        for (Map.Entry<String, Integer> linea : agrupar(items).entrySet()) {
            Optional<Producto> encontrado = repositorio.findById(linea.getKey());
            int solicitado = linea.getValue();
            if (encontrado.isEmpty()) {
                problemas.add(new ProblemaStock(linea.getKey(), "", solicitado, 0, MotivoProblema.NO_EXISTE));
                continue;
            }
            Producto p = encontrado.get();
            if (!p.isActivo()) {
                problemas.add(new ProblemaStock(p.getId(), p.getNombre(), solicitado, p.getStock(), MotivoProblema.INACTIVO));
            } else if (p.getStock() < solicitado) {
                problemas.add(new ProblemaStock(p.getId(), p.getNombre(), solicitado, p.getStock(), MotivoProblema.SIN_STOCK));
            }
        }
        return new DisponibilidadResponse(problemas.isEmpty(), problemas);
    }

    /**
     * Descuenta el stock de una compra confirmada. Es todo o nada (@Transactional):
     * si una línea no alcanza, no se descuenta ninguna y se responde 409.
     */
    @Transactional
    public List<Producto> descontarStock(List<ItemStockRequest> items) {
        DisponibilidadResponse revision = verificarDisponibilidad(items);
        if (!revision.disponible()) {
            ProblemaStock primero = revision.problemas().get(0);
            String nombre = primero.nombre().isBlank() ? primero.productoId() : primero.nombre();
            throw new ConflictoException("Stock insuficiente para \"" + nombre + "\": quedan " + primero.disponible());
        }
        List<Producto> actualizados = new ArrayList<>();
        for (Map.Entry<String, Integer> linea : agrupar(items).entrySet()) {
            Producto p = obtener(linea.getKey());
            p.setStock(p.getStock() - linea.getValue());
            actualizados.add(repositorio.save(p));
        }
        return actualizados;
    }

    /** Siguiente id libre de la categoría: c01…c04 → "c05"; m01k, m02k → "m03k". Misma regla que IdsProducto en la app. */
    String siguienteId(Categoria categoria) {
        Pattern formato = Pattern.compile("^" + categoria.getPrefijo() + "(\\d+)" + categoria.getSufijo() + "$");
        int mayor = 0;
        for (Producto producto : repositorio.findAll()) {
            Matcher m = formato.matcher(producto.getId());
            if (m.matches()) mayor = Math.max(mayor, Integer.parseInt(m.group(1)));
        }
        return String.format("%s%02d%s", categoria.getPrefijo(), mayor + 1, categoria.getSufijo());
    }

    /** Si el mismo producto viene en dos líneas, se suman las cantidades. */
    private static Map<String, Integer> agrupar(List<ItemStockRequest> items) {
        Map<String, Integer> total = new LinkedHashMap<>();
        for (ItemStockRequest item : items) total.merge(item.productoId(), item.cantidad(), Integer::sum);
        return total;
    }

    /** Los cursos no se envían (sin peso); los demás productos necesitan peso para calcular el envío. */
    private static void validarPeso(ProductoRequest datos) {
        if (datos.categoria() != Categoria.CURSO && datos.pesoKg() == null) {
            throw new DatoInvalidoException("pesoKg", "El peso es obligatorio para productos con envío");
        }
    }

    private static void copiar(ProductoRequest datos, Producto producto) {
        producto.setNombre(datos.nombre().trim());
        producto.setCategoria(datos.categoria());
        producto.setPrecio(datos.precio());
        producto.setStock(datos.stock());
        producto.setPesoKg(datos.categoria() == Categoria.CURSO ? null : datos.pesoKg());
        producto.setDescripcion(datos.descripcion() == null ? "" : datos.descripcion().trim());
    }
}
