package co.edu.tecnologicaoriente.sigr.security;

import co.edu.tecnologicaoriente.sigr.model.Usuario;
import co.edu.tecnologicaoriente.sigr.repository.UsuarioRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class DbUserDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;

    public DbUserDetailsService(UsuarioRepository repo) { this.repo = repo; }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Usuario u = repo.findByUsuario(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
        var authorities = List.of(new SimpleGrantedAuthority(u.getRol().authority()));
        return User.withUsername(u.getUsuario())
                .password(u.getContrasena())
                .authorities(authorities)
                .disabled(!u.isActivo())
                .build();
    }
}
