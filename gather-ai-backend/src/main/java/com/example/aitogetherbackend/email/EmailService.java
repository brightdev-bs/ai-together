package com.example.aitogetherbackend.email;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    @Value("${front-server}")
    private String FRONT_SERVER;

    private final JavaMailSender emailSender;

    @Async
    public void sendEmail(String email, String filename) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("AI-Together Image Result");
        mailMessage.setText(FRONT_SERVER + "/result?id=" + filename);
        emailSender.send(mailMessage);
    }
}
