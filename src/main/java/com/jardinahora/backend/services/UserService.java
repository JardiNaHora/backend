package com.jardinahora.backend.services;

import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.responses.BaseResponse;

import java.util.Optional;

public interface UserService {

    Optional<User> findByEmail(String email);

    void save(User user);

    BaseResponse registerAccount(UserDTO userDTO);
    
    /**
     * Confirma o cadastro do usuário usando o token de confirmação
     * 
     * @param token Token de confirmação
     * @return Resposta com status da operação
     */
    BaseResponse confirmEmail(String token);
    
    /**
     * Reenvia e-mail de confirmação
     * 
     * @param email E-mail do usuário
     * @return Resposta com status da operação
     */
    BaseResponse resendConfirmationEmail(String email);

}
