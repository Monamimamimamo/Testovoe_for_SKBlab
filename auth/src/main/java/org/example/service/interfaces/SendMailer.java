package org.example.service.interfaces;

import org.example.domain.SignupResponse;

import javax.mail.MessagingException;

public interface SendMailer {


    /**
     * Отправка сообщения на почту.
     *
     * @param messageBody - сохранённая в БД форма регистрации.
     * Сообщение отправляется на указанный в форме email.
     * Опправляется с почты, указанной в application.yml.
     */
    void send (SignupResponse messageBody) throws MessagingException;
}