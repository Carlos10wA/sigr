package co.edu.tecnologicaoriente.sigr.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Etapa 2: autenticación desde base de datos (DbUserDetailsService + BCrypt).
 * CSRF habilitado por defecto; cierre de sesión por POST.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login", "/css/**", "/js/**", "/error", "/error/**").permitAll()
                .requestMatchers("/usuarios/**").hasAuthority("ROLE_ADMINISTRADOR")
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
