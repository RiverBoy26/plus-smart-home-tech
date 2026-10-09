package ru.yandex.practicum.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;

@Configuration
@EnableWebFluxSecurity
public class GatewaySecurityConfig {
    @Value("${app.security.users[0].username}")
    String ivanUsername;

    @Value("${app.security.users[0].password}")
    String ivanPassword;

    @Value("${app.security.users[0].roles[0]}")
    String ivanUserRole;

    @Value("${app.security.users[1].username}")
    String annaUsername;

    @Value("${app.security.users[1].password}")
    String annaPassword;

    @Value("${app.security.users[1].roles[0]}")
    String annaUserRole;

    @Value("${app.security.users[1].roles[1]}")
    String annaAdminRole;

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .authorizeExchange(auth -> auth
                        .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers(HttpMethod.GET,
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/v3/api-docs",
                                "/v3/api-docs/**")
                        .permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/categories/**").permitAll()
                        .pathMatchers(HttpMethod.GET, "/api/inventory/**").permitAll()
                        .pathMatchers(HttpMethod.POST, "/api/orders/**").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/orders/by-email").hasRole("USER")
                        .pathMatchers(HttpMethod.GET, "/api/orders/{id}").hasRole("USER")
                        .pathMatchers(HttpMethod.GET,"/api/orders").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.POST,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/inventory/**")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.PUT,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/inventory/**")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.PATCH,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/inventory/**")
                        .hasRole("ADMIN")

                        .pathMatchers(HttpMethod.DELETE,
                                "/api/products/**",
                                "/api/categories/**",
                                "/api/inventory/**")
                        .hasRole("ADMIN")
                        .anyExchange().denyAll()
                )
                .httpBasic(Customizer.withDefaults())
                .cors(Customizer.withDefaults())
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .securityContextRepository(
                        NoOpServerSecurityContextRepository.getInstance()
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    MapReactiveUserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails ivan = User.withUsername(ivanUsername)
                .password(passwordEncoder.encode(ivanPassword))
                .roles(ivanUserRole)
                .build();

        UserDetails anna = User.withUsername(annaUsername)
                .password(passwordEncoder.encode(annaPassword))
                .roles(annaUserRole, annaAdminRole)
                .build();

        return new MapReactiveUserDetailsService(ivan, anna);
    }
}