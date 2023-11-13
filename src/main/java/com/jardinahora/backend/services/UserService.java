package com.jardinahora.backend.services;

import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.responses.BaseResponse;

import java.util.Optional;

public interface UserService {

    Optional<User> findByEmail(String email);

    void save(User user);

    BaseResponse registerAccount(UserDTO userDTO);

}
