package io.shinmen.taskmaster.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    public void sendVerificationEmail(String toEmail, String token) {
        String subject = "TaskMaster - Email Verification";
        String verificationUrl = frontendUrl + "/verify?token=" + token;
        String message = "Please verify your email by clicking the link below:\n" + verificationUrl;

        sendEmail(toEmail, subject, message);
    }

    public void sendPasswordResetEmail(String toEmail, String token) {
        String subject = "TaskMaster - Password Reset";
        String resetUrl = frontendUrl + "/reset-password?token=" + token;
        String message = "Reset your password by clicking the link below:\n" + resetUrl;

        sendEmail(toEmail, subject, message);
    }

    private void sendEmail(String to, String subject, String body) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setFrom(fromEmail);
        email.setTo(to);
        email.setSubject(subject);
        email.setText(body);

        mailSender.send(email);
    }
}
