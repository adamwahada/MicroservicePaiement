package Projet.Microservice.Configurations;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Optional;

@Configuration
public class AuditConfig {

    @Bean
    public AuditorAware<String> auditorProvider() {
        return () -> {
            try {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

                if (authentication == null || !authentication.isAuthenticated()) {
                    System.out.println("⚠️ No authentication found, using 'system'");
                    return Optional.of("system");
                }

                // Extract from JWT token
                if (authentication.getPrincipal() instanceof Jwt) {
                    Jwt jwt = (Jwt) authentication.getPrincipal();

                    // Try username first
                    String username = jwt.getClaimAsString("preferred_username");
                    if (username != null && !username.isEmpty()) {
                        System.out.println("✅ Auditor: " + username);
                        return Optional.of(username);
                    }

                    // Try email
                    String email = jwt.getClaimAsString("email");
                    if (email != null && !email.isEmpty()) {
                        System.out.println("✅ Auditor (email): " + email);
                        return Optional.of(email);
                    }

                    // Fallback to Keycloak ID
                    String keycloakId = jwt.getSubject();
                    System.out.println("✅ Auditor (keycloak ID): " + keycloakId);
                    return Optional.of(keycloakId);
                }

                // Fallback
                System.out.println("✅ Auditor (name): " + authentication.getName());
                return Optional.of(authentication.getName());

            } catch (Exception e) {
                System.err.println("❌ Error getting auditor: " + e.getMessage());
                return Optional.of("system");
            }
        };
    }
}