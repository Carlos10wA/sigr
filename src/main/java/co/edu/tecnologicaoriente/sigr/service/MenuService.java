package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.dto.CategoriaForm;
import co.edu.tecnologicaoriente.sigr.dto.PlatoForm;
import co.edu.tecnologicaoriente.sigr.model.Categoria;
import co.edu.tecnologicaoriente.sigr.model.Plato;
import co.edu.tecnologicaoriente.sigr.repository.CategoriaRepository;
import co.edu.tecnologicaoriente.sigr.repository.PlatoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MenuService {

    public static class CategoriaDuplicadaException extends RuntimeException {
        public CategoriaDuplicadaException(String m) { super(m); }
    }
    public static class CategoriaInvalidaException extends RuntimeException {
        public CategoriaInvalidaException(String m) { super(m); }
    }

    private final CategoriaRepository categorias;
    private final PlatoRepository platos;

    public MenuService(CategoriaRepository categorias, PlatoRepository platos) {
        this.categorias = categorias;
        this.platos = platos;
    }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategorias() { return categorias.findAll(); }

    @Transactional(readOnly = true)
    public List<Categoria> listarCategoriasActivas() {
        return categorias.findAll().stream().filter(Categoria::isActiva).toList();
    }

    @Transactional(readOnly = true)
    public Categoria buscarCategoria(Long id) {
        return categorias.findById(id)
                .orElseThrow(() -> new CategoriaInvalidaException("La categoría no existe."));
    }

    @Transactional
    public Categoria crearCategoria(CategoriaForm form) {
        if (categorias.existsByNombre(form.getNombre().trim())) {
            throw new CategoriaDuplicadaException("Ya existe una categoría con ese nombre.");
        }
        return categorias.save(new Categoria(form.getNombre().trim(), form.isActiva()));
    }

    @Transactional
    public Categoria editarCategoria(Long id, CategoriaForm form) {
        Categoria c = buscarCategoria(id);
        if (categorias.existsByNombreAndIdNot(form.getNombre().trim(), id)) {
            throw new CategoriaDuplicadaException("Ya existe otra categoría con ese nombre.");
        }
        c.setNombre(form.getNombre().trim());
        c.setActiva(form.isActiva());
        return categorias.save(c);
    }

    @Transactional
    public Categoria cambiarEstadoCategoria(Long id, boolean activa) {
        Categoria c = buscarCategoria(id);
        c.setActiva(activa);
        return categorias.save(c);
    }

    @Transactional(readOnly = true)
    public List<Plato> catalogo(String q, Long categoriaId) {
        boolean hasQ = q != null && !q.isBlank();
        if (hasQ && categoriaId != null) {
            return platos.findByNombreContainingIgnoreCaseAndCategoriaIdAndActivoTrue(q.trim(), categoriaId);
        }
        if (hasQ) {
            return platos.findByNombreContainingIgnoreCaseAndActivoTrue(q.trim());
        }
        if (categoriaId != null) {
            return platos.findByCategoriaIdAndActivoTrue(categoriaId);
        }
        return platos.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Plato> listarPlatosAdmin() { return platos.findAll(); }

    @Transactional(readOnly = true)
    public Plato buscarPlato(Long id) {
        return platos.findById(id)
                .orElseThrow(() -> new CategoriaInvalidaException("El plato no existe."));
    }

    @Transactional
    public Plato crearPlato(PlatoForm form) {
        Categoria cat = categorias.findById(form.getCategoriaId())
                .orElseThrow(() -> new CategoriaInvalidaException("La categoría seleccionada no existe."));
        if (!cat.isActiva()) {
            throw new CategoriaInvalidaException("La categoría seleccionada está desactivada.");
        }
        Plato p = new Plato();
        aplicar(p, form, cat);
        return platos.save(p);
    }

    @Transactional
    public Plato editarPlato(Long id, PlatoForm form) {
        Plato p = buscarPlato(id);
        Categoria cat = categorias.findById(form.getCategoriaId())
                .orElseThrow(() -> new CategoriaInvalidaException("La categoría seleccionada no existe."));
        if (!cat.isActiva()) {
            throw new CategoriaInvalidaException("La categoría seleccionada está desactivada.");
        }
        aplicar(p, form, cat);
        return platos.save(p);
    }

    private void aplicar(Plato p, PlatoForm form, Categoria cat) {
        p.setNombre(form.getNombre().trim());
        p.setDescripcion(form.getDescripcion().trim());
        p.setCategoria(cat);
        p.setPrecio(form.getPrecio());
        String foto = form.getFoto() == null || form.getFoto().isBlank()
                ? "/images/platos/placeholder.jpg" : form.getFoto().trim();
        if (!foto.startsWith("/images/platos/")) {
            foto = "/images/platos/" + foto.replaceFirst("^/*", "");
        }
        p.setFoto(foto);
        p.setDisponible(form.isDisponible());
        p.setActivo(form.isActivo());
    }

    @Transactional
    public Plato cambiarEstadoPlato(Long id, boolean activo) {
        Plato p = buscarPlato(id);
        p.setActivo(activo);
        return platos.save(p);
    }

    @Transactional
    public Plato cambiarDisponibilidad(Long id, boolean disponible) {
        Plato p = buscarPlato(id);
        // No tiene sentido marcar disponible un plato desactivado.
        if (!p.isActivo() && disponible) {
            throw new CategoriaInvalidaException("Un plato desactivado no puede marcarse como disponible.");
        }
        p.setDisponible(disponible);
        return platos.save(p);
    }
}
