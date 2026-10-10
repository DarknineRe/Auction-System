package com.example.project.config;

import java.util.Locale;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.AnyRequestMatcher;

import com.example.project.repository.UserRepository;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            RestAuthenticationEntryPoint authenticationEntryPoint,
            RestAccessDeniedHandler accessDeniedHandler,
            SecurityContextRepository securityContextRepository,
            UserRepository userRepository,
            UserDetailsService userDetailsService,
            SessionRegistry sessionRegistry,
            @Value("${app.remember-me.key:}") String rememberMeKey) throws Exception {
        http
                .csrf(Customizer.withDefaults())
                .securityContext(context -> context.securityContextRepository(securityContextRepository))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.GET, "/", "/home", "/login", "/register",
                                "/biddings/**", "/sellers/*", "/css/**", "/js/**", "/favicon.ico", "/error")
                        .permitAll()
                        .requestMatchers(HttpMethod.POST, "/register").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/users").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/admin/**").access(new CurrentAdminAuthorizationManager(userRepository))
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "SUPER_ADMIN")
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/biddings/mine",
                                "/api/v1/biddings/won",
                                "/api/v1/users/me/bids")
                        .authenticated()
                        .requestMatchers(HttpMethod.GET,
                                "/api/v1/artworks/**",
                                "/api/v1/biddings",
                                "/api/v1/biddings/*",
                                "/api/v1/biddings/*/bids",
                                "/api/v1/biddings/*/bids/highest",
                                "/api/v1/biddings/*/comments",
                                "/api/v1/seller-profiles/users/*")
                        .permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(new EnabledAccountFilter(userRepository, sessionRegistry), AuthorizationFilter.class)
                .httpBasic(basic -> basic.authenticationEntryPoint(authenticationEntryPoint))
                .sessionManagement(session -> session
                        .maximumSessions(1)
                        .sessionRegistry(sessionRegistry)
                        .maxSessionsPreventsLogin(false))
                .formLogin(form -> form
                        .loginPage("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/")
                        .failureUrl("/login?error")
                        .permitAll())
                .rememberMe(remember -> remember
                        .rememberMeParameter("remember")
                        .userDetailsService(userDetailsService)
                        // Without a configured key, tokens stop working after a restart.
                        .key(rememberMeKey.isBlank() ? UUID.randomUUID().toString() : rememberMeKey))
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .headers(headers -> headers
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .preload(true)
                                .maxAgeInSeconds(31536000)))
                .exceptionHandling(ex -> ex
                        // API clients get a JSON 401; browser pages are redirected to the login form.
                        .defaultAuthenticationEntryPointFor(authenticationEntryPoint,
                                PathPatternRequestMatcher.withDefaults().matcher("/api/**"))
                        .defaultAuthenticationEntryPointFor(new LoginUrlAuthenticationEntryPoint("/login"),
                                AnyRequestMatcher.INSTANCE)
                        .accessDeniedHandler(accessDeniedHandler));

        return http.build();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SessionRegistry sessionRegistry() {
        return new SessionRegistryImpl();
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .map(user -> User.withUsername(user.getEmail())
                        .password(user.getPassword())
                        .roles((user.getRole() == null
                                ? com.example.project.model.User.Role.USER
                                : user.getRole()).name())
                                  .disabled(!user.isEnabled())
                        .build())
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }
}