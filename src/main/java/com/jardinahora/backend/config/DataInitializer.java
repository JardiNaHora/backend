package com.jardinahora.backend.config;

import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.models.Vehicle;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.repositories.VehicleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Garante que os papéis (USER e ADMIN) existam no banco ao subir a aplicação.
 * Assim não é necessário criar tabelas nem dados iniciais manualmente.
 */
@Component
@Order(1)
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRoleRepository userRoleRepository;
    private final UserRepository userRepository;
    private final VehicleRepository vehicleRepository;
    private final org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        createRoleIfMissing("USER");
        createRoleIfMissing("ADMIN");
        createDevUserIfMissing();
        createDevVehicleIfMissing();
    }

    private void createRoleIfMissing(String roleName) {
        if (userRoleRepository.findByName(roleName) == null) {
            UserRole role = new UserRole();
            role.setName(roleName);
            userRoleRepository.save(role);
            log.info("Papel '{}' criado automaticamente.", roleName);
        }
    }

    /**
     * Cria um usuário de desenvolvimento padrão, caso não exista nenhum usuário.
     * Este usuário é útil para testes locais de autenticação e fluxos protegidos.
     */
    private void createDevUserIfMissing() {
        if (userRepository.count() == 0) {
            User user = new User();
            user.setUsername("dev@jardinahora.local");
            user.setEmail("dev@jardinahora.local");
            // senha "dev123" gerada com o mesmo encoder da aplicação
            user.setPassword(passwordEncoder.encode("dev123"));
            user.setEmailConfirmed(true);
            user.setEnabled(true);
            user.setAccountNonExpired(true);
            user.setAccountNonLocked(true);
            user.setCredentialsNonExpired(true);

            UserRole userRole = userRoleRepository.findByName("USER");
            user.setRoles(new java.util.HashSet<>());
            user.getRoles().add(userRole);

            userRepository.save(user);
            log.info("Usuário de desenvolvimento 'dev@jardinahora.local' criado com senha 'dev123'.");
        }
    }

    private void createDevVehicleIfMissing() {
        if (vehicleRepository.count() == 0) {
            Vehicle v = new Vehicle();
            v.setName("JARDINEIRA-01");
            v.setType("JARDINEIRA");
            v.setPlate("AAA0A00");
            v.setPassengers(35);
            vehicleRepository.save(v);
            log.info("Veículo de desenvolvimento '{}' criado automaticamente.", v.getName());
        }
    }
}
