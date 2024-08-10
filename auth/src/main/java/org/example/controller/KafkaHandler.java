package org.example.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.domain.SignupResponse;
import org.example.service.AuthServiceImpl;
import org.example.service.SendMailerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class KafkaHandler {
    @Autowired
    private SendMailerImpl sendMailer;

    @Autowired
    private AuthServiceImpl service;

    @KafkaListener(topics = "signup-response-topic", groupId = "group_id")
    public void handleSignupResponse(ConsumerRecord<String, SignupResponse> record) {
        try {
            SignupResponse response = record.value();
            log.info("Kafka приняла сообщение: {}, на топике: {}", response, record.topic());
            service.changeFormStatus(response.getSuccessfully(), response.getRequest().getEmail());
            sendMailer.send(response);
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения Kafka: {}", e.getMessage());
        }
    }
}
