package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.model.Categoria;
import co.edu.tecnologicaoriente.sigr.model.Plato;
import co.edu.tecnologicaoriente.sigr.repository.CategoriaRepository;
import co.edu.tecnologicaoriente.sigr.repository.PlatoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Carga explícita e idempotente del catálogo de demostración.
 * Solo se ejecuta al pulsar el botón en la interfaz (POST /platos/cargar-demo).
 * Si ya existe la categoría o el plato, lo omite: no duplica ni sobrescribe.
 */
@Service
public class DemoDataService {

    private final CategoriaRepository categorias;
    private final PlatoRepository platos;

    public DemoDataService(CategoriaRepository categorias, PlatoRepository platos) {
        this.categorias = categorias;
        this.platos = platos;
    }

    public record ResultadoDemo(int categoriasNuevas, int platosNuevos) {}

    @Transactional
    public ResultadoDemo cargarDemo() {
        int catNuevas = 0, platosNuevos = 0;
        catNuevas += crearCategoriaSiNoExiste("Entradas");
        catNuevas += crearCategoriaSiNoExiste("Platos principales");
        catNuevas += crearCategoriaSiNoExiste("Bebidas");
        catNuevas += crearCategoriaSiNoExiste("Postres");

        platosNuevos += crearPlatoSiNoExiste("Empanadas", "Empanadas doradas de carne con ají de la casa.", "Entradas", "12000", "/images/platos/empanadas.jpg");
        platosNuevos += crearPlatoSiNoExiste("Ceviche", "Ceviche fresco de pescado con limón y cebolla roja.", "Entradas", "18000", "/images/platos/ceviche.jpg");
        platosNuevos += crearPlatoSiNoExiste("Arepas rellenas", "Arepa asada con queso y chorizo.", "Entradas", "15000", "/images/platos/arepas.jpg");
        platosNuevos += crearPlatoSiNoExiste("Bandeja paisa", "Bandeja completa con arroz, frijoles, huevo, chicharrón y aguacate.", "Platos principales", "28000", "/images/platos/bandeja-paisa.jpg");
        platosNuevos += crearPlatoSiNoExiste("Pollo a la plancha", "Pechuga a la plancha con papas y ensalada fresca.", "Platos principales", "22000", "/images/platos/pollo-plancha.jpg");
        platosNuevos += crearPlatoSiNoExiste("Pasta alfredo", "Pasta cremosa con pollo y parmesano.", "Platos principales", "24000", "/images/platos/pasta-alfredo.jpg");
        platosNuevos += crearPlatoSiNoExiste("Jugo de mango", "Jugo natural de mango, recién licuado.", "Bebidas", "7000", "/images/platos/jugo-mango.jpg");
        platosNuevos += crearPlatoSiNoExiste("Café de la casa", "Café colombiano recién preparado.", "Bebidas", "5000", "/images/platos/cafe.jpg");
        platosNuevos += crearPlatoSiNoExiste("Limonada natural", "Limonada con yerbabuena.", "Bebidas", "8000", "/images/platos/limonada.jpg");
        platosNuevos += crearPlatoSiNoExiste("Torta de chocolate", "Torta de chocolate con cubierta cremosa.", "Postres", "11000", "/images/platos/torta-chocolate.jpg");
        platosNuevos += crearPlatoSiNoExiste("Flan", "Flan casero con caramelo.", "Postres", "9000", "/images/platos/flan.jpg");
        platosNuevos += crearPlatoSiNoExiste("Helado", "Copa de helado artesanal.", "Postres", "10000", "/images/platos/helado.jpg");

        return new ResultadoDemo(catNuevas, platosNuevos);
    }

    private int crearCategoriaSiNoExiste(String nombre) {
        if (categorias.existsByNombre(nombre)) return 0;
        categorias.save(new Categoria(nombre, true));
        return 1;
    }

    private int crearPlatoSiNoExiste(String nombre, String desc, String catNombre, String precio, String foto) {
        if (platos.existsByNombre(nombre)) return 0;
        Categoria cat = categorias.findByNombre(catNombre).orElseThrow();
        Plato p = new Plato();
        p.setNombre(nombre);
        p.setDescripcion(desc);
        p.setCategoria(cat);
        p.setPrecio(new BigDecimal(precio));
        p.setFoto(foto);
        p.setDisponible(true);
        p.setActivo(true);
        platos.save(p);
        return 1;
    }
}
