package com.jardinahora.backend.services;

/**
 * Interface para serviço de envio de e-mails
 */
public interface EmailService {
    
    /**
     * Envia e-mail de confirmação de cadastro
     * 
     * @param to E-mail do destinatário
     * @param confirmationToken Token de confirmação
     * @param confirmationUrl URL completa para confirmação
     */
    void sendConfirmationEmail(String to, String confirmationToken, String confirmationUrl);
    
    /**
     * Envia e-mail genérico
     * 
     * @param to E-mail do destinatário
     * @param subject Assunto do e-mail
     * @param body Corpo do e-mail
     */
    void sendEmail(String to, String subject, String body);
}
