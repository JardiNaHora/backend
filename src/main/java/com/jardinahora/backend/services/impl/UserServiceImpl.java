package com.jardinahora.backend.services.impl;

import com.jardinahora.backend.dtos.UserDTO;
import com.jardinahora.backend.exceptions.BaseException;
import com.jardinahora.backend.models.Provider;
import com.jardinahora.backend.models.User;
import com.jardinahora.backend.models.UserRole;
import com.jardinahora.backend.repositories.UserRepository;
import com.jardinahora.backend.repositories.UserRoleRepository;
import com.jardinahora.backend.responses.BaseResponse;
import com.jardinahora.backend.services.EmailService;
import com.jardinahora.backend.services.UserService;
import com.jardinahora.backend.utils.TokenGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    private final UserRoleRepository roleRepository;

    private final BCryptPasswordEncoder passwordEncoder;
    
    private final EmailService emailService;
    
    @Value("${frontend.url}")
    private String frontendUrl;
    
    private static final int TOKEN_EXPIRATION_HOURS = 24;

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public void save(User user) {
        userRepository.save(user);
    }

    @Override
    @Transactional
    public BaseResponse registerAccount(UserDTO userDTO) {
        BaseResponse response = new BaseResponse();

        //validate data from client
        validateAccount(userDTO);

        User user = insertUser(userDTO);
        
        // Gera token de confirmação
        String confirmationToken = TokenGenerator.generateSecureToken();
        user.setConfirmationToken(confirmationToken);
        
        // Define expiração do token (24 horas)
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, TOKEN_EXPIRATION_HOURS);
        user.setTokenExpiration(calendar.getTime());
        
        // Usuário não confirmado inicialmente
        user.setEmailConfirmed(false);
        user.setEnabled(false); // Desabilita até confirmar e-mail

        try {
            userRepository.save(user);
            
            // Envia e-mail de confirmação
            String confirmationUrl = frontendUrl + "/confirm-email?token=" + confirmationToken;
            emailService.sendConfirmationEmail(user.getUsername(), confirmationToken, confirmationUrl);
            
            response.setCode(String.valueOf(HttpStatus.CREATED.value()));
            response.setMessage("Cadastro realizado com sucesso! Por favor, verifique seu e-mail para confirmar sua conta.");
            
            log.info("Usuário registrado: {} - Token gerado: {}", user.getUsername(), confirmationToken);
        } catch (Exception e) {
            log.error("Erro ao registrar usuário: {}", e.getMessage(), e);
            response.setCode(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()));
            response.setMessage("Erro ao processar cadastro. Tente novamente mais tarde.");
        }
        return response;
    }

    private User insertUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));

        Set<UserRole> roles = new HashSet<>();
        roles.add(roleRepository.findByName(userDTO.getRole()));
        user.setRoles(roles);

        user.setEnabled(true);
        user.setAccountNonExpired(true);
        user.setAccountNonLocked(true);
        user.setCredentialsNonExpired(true);

        user.setProviderId(Provider.local.name());

        return user;
    }

    private void validateAccount(UserDTO userDTO){
        if(ObjectUtils.isEmpty(userDTO)){
            throw new BaseException(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Request data not found!");
        }

        try {
            if(!ObjectUtils.isEmpty(userDTO.checkProperties())){
                throw new BaseException(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Request data not found!");
            }
        }catch (IllegalAccessException e){
            throw new BaseException(String.valueOf(HttpStatus.SERVICE_UNAVAILABLE.value()), "Service Unavailable");
        }

        List<String> roles = roleRepository.findAll().stream().map(UserRole::getName).toList();

        if(!roles.contains(userDTO.getRole())){
            throw new BaseException(String.valueOf(HttpStatus.BAD_REQUEST.value()), "Invalid role");
        }

        User user = userRepository.findByUsername(userDTO.getUsername());

        if(!ObjectUtils.isEmpty(user)){
            throw new BaseException(String.valueOf(HttpStatus.BAD_REQUEST.value()), "User had existed!!!");
        }

    }
    
    @Override
    @Transactional
    public BaseResponse confirmEmail(String token) {
        BaseResponse response = new BaseResponse();
        
        if (token == null || token.isEmpty()) {
            response.setCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.setMessage("Token de confirmação inválido.");
            return response;
        }
        
        Optional<User> userOpt = userRepository.findByConfirmationToken(token);
        
        if (userOpt.isEmpty()) {
            response.setCode(String.valueOf(HttpStatus.NOT_FOUND.value()));
            response.setMessage("Token de confirmação inválido ou expirado.");
            return response;
        }
        
        User user = userOpt.get();
        
        // Verifica se o token expirou
        if (user.getTokenExpiration() != null && user.getTokenExpiration().before(new Date())) {
            response.setCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.setMessage("Token de confirmação expirado. Solicite um novo e-mail de confirmação.");
            return response;
        }
        
        // Verifica se já foi confirmado
        if (user.isEmailConfirmed()) {
            response.setCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.setMessage("E-mail já foi confirmado anteriormente.");
            return response;
        }
        
        // Confirma o e-mail e ativa a conta
        user.setEmailConfirmed(true);
        user.setEnabled(true);
        user.setConfirmationToken(null); // Remove o token após confirmação
        user.setTokenExpiration(null);
        
        userRepository.save(user);
        
        log.info("E-mail confirmado com sucesso para usuário: {}", user.getUsername());
        
        response.setCode(String.valueOf(HttpStatus.OK.value()));
        response.setMessage("E-mail confirmado com sucesso! Sua conta foi ativada.");
        
        return response;
    }
    
    @Override
    @Transactional
    public BaseResponse resendConfirmationEmail(String email) {
        BaseResponse response = new BaseResponse();
        
        if (email == null || email.isEmpty()) {
            response.setCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.setMessage("E-mail é obrigatório.");
            return response;
        }
        
        Optional<User> userOpt = userRepository.findByEmail(email);
        
        if (userOpt.isEmpty()) {
            // Por segurança, não revela se o e-mail existe ou não
            response.setCode(String.valueOf(HttpStatus.OK.value()));
            response.setMessage("Se o e-mail estiver cadastrado, um novo link de confirmação será enviado.");
            return response;
        }
        
        User user = userOpt.get();
        
        // Se já está confirmado, não precisa reenviar
        if (user.isEmailConfirmed()) {
            response.setCode(String.valueOf(HttpStatus.BAD_REQUEST.value()));
            response.setMessage("Este e-mail já foi confirmado.");
            return response;
        }
        
        // Gera novo token
        String confirmationToken = TokenGenerator.generateSecureToken();
        user.setConfirmationToken(confirmationToken);
        
        // Define nova expiração
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR_OF_DAY, TOKEN_EXPIRATION_HOURS);
        user.setTokenExpiration(calendar.getTime());
        
        userRepository.save(user);
        
        // Envia novo e-mail
        String confirmationUrl = frontendUrl + "/confirm-email?token=" + confirmationToken;
        emailService.sendConfirmationEmail(user.getUsername(), confirmationToken, confirmationUrl);
        
        log.info("E-mail de confirmação reenviado para: {}", email);
        
        response.setCode(String.valueOf(HttpStatus.OK.value()));
        response.setMessage("Um novo e-mail de confirmação foi enviado. Verifique sua caixa de entrada.");
        
        return response;
    }

}
