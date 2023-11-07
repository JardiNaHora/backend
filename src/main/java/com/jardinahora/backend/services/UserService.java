package com.jardinahora.backend.services;

import com.jardinahora.backend.models.User;

import java.util.Optional;

public interface UserService {

    Optional<User> findByEmail(String email);

    void save(User user);

}
