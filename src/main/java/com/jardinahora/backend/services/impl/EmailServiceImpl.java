package com.jardinahora.backend.services.impl;

import com.jardinahora.backend.services.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

/**
 * Implementação do serviço de envio de e-mails
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${frontend.url}")
    private String frontendUrl;

    @Value("${app.name:JardiNaHora}")
    private String appName;

    @Override
    public void sendConfirmationEmail(String to, String confirmationToken, String confirmationUrl) {
        try {
            Context context = new Context();
            context.setVariable("confirmationUrl", confirmationUrl);
            context.setVariable("appName", appName);
            context.setVariable("token", confirmationToken);
            
            String htmlContent = templateEngine.process("email/confirmation-email", context);
            
            sendEmail(to, "Confirme seu cadastro no " + appName, htmlContent);
            
            log.info("E-mail de confirmação enviado para: {}", to);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail de confirmação para: {}", to, e);
            throw new RuntimeException("Erro ao enviar e-mail de confirmação", e);
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true indica que é HTML
            
            mailSender.send(message);
            
            log.info("E-mail enviado com sucesso para: {}", to);
        } catch (MessagingException e) {
            log.error("Erro ao enviar e-mail para: {}", to, e);
            throw new RuntimeException("Erro ao enviar e-mail", e);
        }
    }
}
