package org.example;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.domain.SignupRequest;
import org.example.domain.SignupResponse;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ThreadLocalRandom;

@RestController
@Slf4j
public class KafkaHandler {


    @KafkaListener(topics = "signup-request-topic", groupId = "group_id")
    @SendTo("signup-response-topic")
    public SignupResponse handleSignupRequest(ConsumerRecord<String, SignupRequest> record) {
        log.info("Принято сообщение: " + record);
        SignupRequest request = record.value();
        try {
            Thread.sleep(ThreadLocalRandom.current().nextInt(10000, 30000));
            Boolean success = ThreadLocalRandom.current().nextBoolean();
            SignupResponse response = SignupResponse.builder()
                    .successfully(success)
                    .body(success ? "Будем рады вашему присутствию на нашем сервисе!" : "Рандом не зарандомился")
                    .request(request).build();

            log.info("Отправлено сообщение: " + response);
            return response;
        } catch (Exception e) {
            log.error("Ошибка при обработке сообщения Kafka: {}", e.getMessage());
            return new SignupResponse(false, "Проблемы на стороне Kafka", request);
        }
    }
}
