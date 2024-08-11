package org.example.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.SignupForm;
import org.example.domain.SignupRequest;
import org.example.domain.SignupResponse;
import org.example.domain.repo.RegisterFormRepo;
import org.example.kafka.KafkaServiceImpl;
import org.example.service.interfaces.AuthService;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.persistence.PersistenceException;
import javax.validation.ConstraintViolationException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final RegisterFormRepo registerFormRepo;
    private final KafkaServiceImpl kafkaService;

    @Override
    public ResponseEntity<SignupForm> signup(SignupRequest request) {
        try {
            SignupForm registerForm = createAndSaveForm(request);
            log.info("Сохранена форма: {}", registerForm);
            kafkaService.sendMessage(request, "signup-request-topic");
            return ResponseEntity.ok(registerForm);
        } catch (Exception e) {
            if (e instanceof DataAccessException) {
                log.error("Форма с такой почтой уже существует: {}", e.getMessage());
                return new ResponseEntity<>(HttpStatus.CONFLICT);
            }
            log.error("Ошибка при регистрации: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    @Override
    public ResponseEntity<SignupForm> getForm(UUID id) {
        Optional<SignupForm> form = registerFormRepo.findById(id);
        if (!form.isPresent()) {
            log.warn("Форма с ID {} не найдена", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return ResponseEntity.ok(form.get());
    }

    @Override
    public void changeFormStatus(Boolean successfully, String email) {
        Optional<SignupForm> response = registerFormRepo.findByEmail(email);
        if (response.isPresent()) {
            SignupForm form = response.get();
            if (Objects.equals(form.getStatus(), "waiting")) {
                form.setStatus(successfully ? "accepted" : "refused");
                registerFormRepo.save(form);
                log.info("Изменена запись: {}", form);
            } else {
                log.info("Запись не изменена: Запись не находится в статусе \"waiting\"");
                throw new IllegalArgumentException("Запись не изменена: Запись не находится в статусе \"waiting\"");
            }
        } else {
            log.error("Не найдена форма от предложенного email: {}", email);
            throw new IllegalArgumentException("Не найдена форма от предложенного email");
        }
    }

    private SignupForm createAndSaveForm(SignupRequest request) {
        SignupForm registerForm = new SignupForm();
        registerForm.setEmail(request.getEmail());
        registerForm.setLogin(request.getLogin());
        registerForm.setPassword(request.getPassword());
        registerForm.setFullName(request.getFullName());
        registerForm.setStatus("waiting");
        registerFormRepo.save(registerForm);
        return registerForm;
    }
}