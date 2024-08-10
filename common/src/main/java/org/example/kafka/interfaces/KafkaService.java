package org.example.kafka.interfaces;

import org.example.domain.SignupRequest;

public interface KafkaService {

    /**
     * Отправка сообщения в kafka.
     *
     * @param msg форма регистрации для отправки в брокер.
     * @param topic на какой топик отправляем.
     */
    void sendMessage(SignupRequest msg, String topic);
}