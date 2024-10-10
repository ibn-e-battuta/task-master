package io.shinmen.taskmaster.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${server.port}")
    private String port;

    public void sendVerificationEmail(final String to, final String token) {
        String url = "http://localhost:" + port + "/api/auth/verify?token=" + token;
        log.info("Hi {}, to confirm your account, please click here : {}", to, url);
    }

    public void sendPasswordResetEmail(final String to, final String token) {
        String url = "http://localhost:" + port + "/api/auth/reset-password?token=" + token;
        log.info("Hi {}, to reset your account, please click here : {}", to, url);
    }
}
