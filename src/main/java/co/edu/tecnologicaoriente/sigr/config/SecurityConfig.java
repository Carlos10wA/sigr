package co.edu.tecnologicaoriente.sigr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Etapa 3: permisos de catálogo con @PreAuthorize en controladores.
 * /usuarios y /categorias: solo ADMINISTRADOR. /platos: lectura para todo
 * rol autenticado; escritura con @PreAuthorize.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/images/**", "/error", "/error/**").permitAll()
                .requestMatchers("/usuarios/**").hasAuthority("ROLE_ADMINISTRADOR")
                .requestMatchers("/categorias/**").hasAuthority("ROLE_ADMINISTRADOR")
                .anyRequest().authenticated()
            )
            .exceptionHandling(ex -> ex.accessDeniedHandler((request, response, denied) -> {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                request.getRequestDispatcher("/error/403").forward(request, response);
            }))
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );
        return http.build();
    }
}
