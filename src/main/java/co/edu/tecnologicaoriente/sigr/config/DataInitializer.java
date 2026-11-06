package co.edu.tecnologicaoriente.sigr.config;

import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Crea el administrador inicial solo si la tabla está vacía.
 * La contraseña se lee de la variable de entorno SIGR_ADMIN_PASSWORD
 * (o propiedad sigr.admin.password). Nunca se escribe en código,
 * documentos, registros ni repositorios. Si no está definida,
 * no se crea ningún usuario y se registra una advertencia sin el secreto.
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;
    private final Environment env;

    public DataInitializer(UsuarioRepository repo, PasswordEncoder encoder, Environment env) {
        this.repo = repo;
        this.encoder = encoder;
        this.env = env;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) {
            return;
        }
        String password = env.getProperty("sigr.admin.password");
        if (password == null || password.isBlank()) {
            log.warn("SIGR: no hay usuarios y SIGR_ADMIN_PASSWORD no está definida. "
                    + "Defina la variable de entorno y reinicie para crear el administrador inicial. "
                    + "No se creó ningún usuario.");
            return;
        }
        Usuario admin = new Usuario("Administrador SIGR", "admin",
                encoder.encode(password), Rol.ADMINISTRADOR, true);
        repo.save(admin);
        log.info("SIGR: administrador inicial creado desde variable de entorno.");
    }
}
