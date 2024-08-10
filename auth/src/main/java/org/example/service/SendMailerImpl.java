package org.example.service;

import lombok.extern.slf4j.Slf4j;
import org.example.domain.SignupRequest;
import org.example.domain.SignupResponse;
import org.example.service.interfaces.SendMailer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Service
@Slf4j
public class SendMailerImpl implements SendMailer {

    @Autowired private JavaMailSender mailSender;
    @Value("${mail.username}") private String username;

    private static final String SUCCESS_BODY = "%s, вы зарегистрированы!\n\nВаши данные:\nПочта: %s\nЛогин: %s\nПароль: %s\n\nСообщение: %s";
    private static final String SUCCESS_THEME = "%s, вы зарегистрированы!";
    private static final String FAILURE_BODY = "%s, ваш запрос на регистрацию отклонён :(\n\nВаши данные:\nПочта: %s\nЛогин: %s\nПароль: %s\n\nПричина отказа: %s";
    private static final String FAILURE_THEME = "%s, ваш запрос на регистрацию отклонён :(";

    @Override
    public void send(SignupResponse messageBody) throws MessagingException {
        SignupRequest form = messageBody.getRequest();
        String toAdress = form.getEmail();
        String theme;
        String body;
        if (messageBody.getSuccessfully()) {
            theme = String.format(SUCCESS_THEME,form.getFullName());
            body = String.format(SUCCESS_BODY, form.getFullName(), toAdress, form.getLogin(), form.getPassword(), messageBody.getBody());
        } else {
            theme = String.format(FAILURE_THEME, form.getFullName());
            body = String.format(FAILURE_BODY, form.getFullName(), toAdress, form.getLogin(), form.getPassword(), messageBody.getBody());
        }
        sendMail(toAdress, theme, body);
    }


    private void sendMail(String toAddress, String subject, String messageBody) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(message, false, "UTF-8");
        mimeMessageHelper.setTo(toAddress);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(messageBody);
        mimeMessageHelper.setFrom(username);
        log.info("Отправляем сообщение {}, на почту {}", toAddress, messageBody);
        mailSender.send(message);
    }
}