package com.jardinahora.backend.repositories;

import com.jardinahora.backend.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String emailId);

    User findByUsername(String username);

    Optional<User> findByUsernameAndProviderId(String username, String providerId);

}
