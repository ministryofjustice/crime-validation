package uk.gov.justice.laa.crime.validation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationEntryPoint;
import org.springframework.security.oauth2.server.resource.web.access.BearerTokenAccessDeniedHandler;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@Order(1)
@EnableWebSecurity
public class ResourceServerConfiguration {

    public static final String SCOPE_VALIDATION_STANDARD = "SCOPE_validation/standard";

    @Bean
    protected BearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint() {
        BearerTokenAuthenticationEntryPoint bearerTokenAuthenticationEntryPoint = new BearerTokenAuthenticationEntryPoint();
        bearerTokenAuthenticationEntryPoint.setRealmName("Crime Validation API");
        return bearerTokenAuthenticationEntryPoint;
    }

    @Bean
    public AccessDeniedHandler bearerTokenAccessDeniedHandler() {
        return new BearerTokenAccessDeniedHandler();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/oauth2/**").permitAll()
                        .requestMatchers("/open-api/**").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        .requestMatchers("/api/**").hasAuthority(SCOPE_VALIDATION_STANDARD)
                        .anyRequest().authenticated())
                .oauth2ResourceServer((oauth2) -> oauth2
                        .accessDeniedHandler(bearerTokenAccessDeniedHandler())
                        .authenticationEntryPoint(bearerTokenAuthenticationEntryPoint())
                        .jwt(withDefaults()));
        return http.build();
    }

}