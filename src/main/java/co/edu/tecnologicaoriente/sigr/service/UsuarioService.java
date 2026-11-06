package co.edu.tecnologicaoriente.sigr.service;

import co.edu.tecnologicaoriente.sigr.dto.UsuarioCrearForm;
import co.edu.tecnologicaoriente.sigr.dto.UsuarioEditarForm;
import co.edu.tecnologicaoriente.sigr.model.Rol;
import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    public static class UsuarioDuplicadoException extends RuntimeException {
        public UsuarioDuplicadoException(String msg) { super(msg); }
    }
    public static class UltimoAdministradorException extends RuntimeException {
        public UltimoAdministradorException(String msg) { super(msg); }
    }

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarTodos() { return repo.findAll(); }

    @Transactional(readOnly = true)
    public Usuario buscarPorId(Long id) {
        return repo.findById(id).orElseThrow(() -> new UsuarioDuplicadoException("Usuario no encontrado."));
    }

    @Transactional(readOnly = true)
    public long contarAdministradoresActivos() {
        return repo.countByRolAndActivoTrue(Rol.ADMINISTRADOR);
    }

    @Transactional
    public Usuario crear(UsuarioCrearForm form) {
        if (repo.existsByUsuario(form.getUsuario())) {
            throw new UsuarioDuplicadoException("Ya existe un usuario con ese nombre de usuario.");
        }
        Usuario u = new Usuario(form.getNombreCompleto(), form.getUsuario(),
                encoder.encode(form.getContrasena()), form.getRol(), form.isActivo());
        return repo.save(u);
    }

    @Transactional
    public Usuario editar(Long id, UsuarioEditarForm form) {
        Usuario u = buscarPorId(id);
        if (repo.existsByUsuarioAndIdNot(form.getUsuario(), id)) {
            throw new UsuarioDuplicadoException("Ya existe otro usuario con ese nombre de usuario.");
        }
        // Protección: no quitar rol/activación al último administrador activo.
        if (u.getRol() == Rol.ADMINISTRADOR && u.isActivo()) {
            boolean pierdeAdmin = form.getRol() != Rol.ADMINISTRADOR || !form.isActivo();
            if (pierdeAdmin && contarAdministradoresActivos() <= 1) {
                throw new UltimoAdministradorException(
                        "No se puede quitar el rol o desactivar al único administrador activo.");
            }
        }
        u.setNombreCompleto(form.getNombreCompleto());
        u.setUsuario(form.getUsuario());
        u.setRol(form.getRol());
        u.setActivo(form.isActivo());
        if (form.getContrasena() != null && !form.getContrasena().isBlank()) {
            u.setContrasena(encoder.encode(form.getContrasena()));
        }
        return repo.save(u);
    }

    @Transactional
    public Usuario cambiarEstado(Long id, boolean activo) {
        Usuario u = buscarPorId(id);
        if (!activo && u.getRol() == Rol.ADMINISTRADOR && u.isActivo()
                && contarAdministradoresActivos() <= 1) {
            throw new UltimoAdministradorException(
                    "No se puede desactivar al único administrador activo.");
        }
        u.setActivo(activo);
        return repo.save(u);
    }
}
