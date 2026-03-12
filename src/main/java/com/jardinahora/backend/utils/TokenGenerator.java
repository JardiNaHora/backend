package com.jardinahora.backend.utils;

import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utilitário para geração de tokens seguros
 */
public class TokenGenerator {
    
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final int TOKEN_LENGTH = 32; // 32 bytes = 256 bits
    
    /**
     * Gera um token seguro aleatório usando Base64
     * 
     * @return Token seguro em formato Base64
     */
    public static String generateSecureToken() {
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Gera um token seguro com tamanho customizado
     * 
     * @param length Tamanho em bytes
     * @return Token seguro em formato Base64
     */
    public static String generateSecureToken(int length) {
        byte[] tokenBytes = new byte[length];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
}
