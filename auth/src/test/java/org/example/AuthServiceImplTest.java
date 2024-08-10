package org.example;


import org.example.domain.SignupForm;
import org.example.domain.SignupRequest;
import org.example.domain.SignupResponse;
import org.example.domain.repo.RegisterFormRepo;
import org.example.kafka.KafkaServiceImpl;
import org.example.kafka.interfaces.KafkaService;
import org.example.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;

public class AuthServiceImplTest {

    @Mock
    private RegisterFormRepo registerFormRepo;

    @Mock
    private KafkaServiceImpl kafkaService;
    @InjectMocks
    private AuthServiceImpl authService;



    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    @Test
    void testSignupSuccess() {
        SignupRequest request = new SignupRequest(); // Инициализируйте объект запроса
        SignupForm registerForm = new SignupForm(); // Инициализируйте форму регистрации
        registerForm.setId(UUID.randomUUID());


        ResponseEntity<SignupResponse> response = authService.signup(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        verify(kafkaService).sendMessage(request, "signup-request-topic");
    }

    @Test
    void testSignupFailure() {
        SignupRequest request = new SignupRequest();

        doThrow(new RuntimeException()).when(kafkaService).sendMessage(any(SignupRequest.class), any(String.class));
        ResponseEntity<?> response = authService.signup(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        verify(kafkaService).sendMessage(request, "signup-request-topic");
    }


    @Test
    void testChangeFormStatusToAccepted() {
        String email = "test@example.com";
        boolean successfully = true;

        SignupForm expectedForm = new SignupForm();
        expectedForm.setEmail(email);
        expectedForm.setStatus("waiting");

        when(registerFormRepo.findByEmail(email)).thenReturn(Optional.of(expectedForm));

        authService.changeFormStatus(successfully, email);
        SignupForm updatedForm = registerFormRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Форма не найдена после изменения статуса"));
        assertThat(updatedForm.getStatus()).isEqualTo("accepted");
    }


    @Test
    void testChangeFormStatusToRefused() {
        String email = "test@example.com";
        boolean successfully = false;

        SignupForm expectedForm = new SignupForm();
        expectedForm.setEmail(email);
        expectedForm.setStatus("waiting");

        when(registerFormRepo.findByEmail(email)).thenReturn(Optional.of(expectedForm));

        authService.changeFormStatus(successfully, email);
        SignupForm updatedForm = registerFormRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Форма не найдена после изменения статуса"));
        assertThat(updatedForm.getStatus()).isEqualTo("refused");
    }


    @Test
    void testChangeFormForChangedForm() {
        String email = "test@example.com";
        boolean successfully = true;

        SignupForm expectedForm = new SignupForm();
        expectedForm.setEmail(email);
        expectedForm.setStatus("waiting");

        when(registerFormRepo.findByEmail(email)).thenReturn(Optional.of(expectedForm));

        authService.changeFormStatus(successfully, email);
        SignupForm updatedForm = registerFormRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Форма не найдена после первого изменения статуса"));
        assertThat(updatedForm.getStatus()).isEqualTo("accepted");

        // Во второй части теста мы ожидаем, что метод changeFormStatus выбросит исключение,
        // но мы хотим, чтобы тест продолжил выполнение. Для этого используем try-catch.
        try {
            authService.changeFormStatus(!successfully, email); // Попытка изменить статус снова
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); // Например, можно просто вывести сообщение об ошибке
        }

        // После обработки исключения мы проверяем, что статус формы остался неизменным
        updatedForm = registerFormRepo.findByEmail(email).orElseThrow(() -> new RuntimeException("Форма не найдена после второго изменения статуса"));
        assertThat(updatedForm.getStatus()).isEqualTo("accepted");
    }





    @Test
    void testGetFormFound() {
        UUID id = UUID.randomUUID();
        SignupForm expectedForm = new SignupForm();
        when(registerFormRepo.findById(id)).thenReturn(Optional.of(expectedForm));

        ResponseEntity<SignupForm> result = authService.getForm(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody()).isEqualTo(expectedForm);
    }

    @Test
    void testGetFormNotFound() {
        UUID id = UUID.randomUUID();
        when(registerFormRepo.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<SignupForm> result = authService.getForm(id);

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(result.getBody()).isNull();
    }
}
