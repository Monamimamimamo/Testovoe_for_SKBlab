package org.example.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.domain.SignupRequest;
import org.example.domain.SignupResponse;
import org.example.kafka.interfaces.KafkaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.requestreply.ReplyingKafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class KafkaServiceImpl implements KafkaService {
    @Autowired private  ReplyingKafkaTemplate<String, SignupRequest, SignupResponse> replyingKafkaTemplate;

    public void sendMessage(SignupRequest registerForm, String topic){
        log.info("Отправка сообщения: {}, на топик: {}", registerForm, topic);
        ProducerRecord<String, SignupRequest> record = new ProducerRecord<>(topic, registerForm);
        replyingKafkaTemplate.send(record);
    }
}
