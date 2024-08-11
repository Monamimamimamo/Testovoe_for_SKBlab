package org.example;


import org.example.domain.SignupForm;
import org.example.domain.SignupRequest;
import org.example.domain.repo.RegisterFormRepo;
import org.example.kafka.KafkaServiceImpl;
import org.example.service.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

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
    void testSignupConflictResponse() {
        SignupRequest request = new SignupRequest();
        request.setLogin("monamimamimamo");
        request.setPassword("aA25800");
        request.setEmail("kirill7195@yandex.ru");
        request.setFullName("Иванов Иван Иванович");
        when(registerFormRepo.save(any())).thenAnswer(invocation -> {
            throw new DataAccessException("Дубликат записи") {};
        });
        ResponseEntity<SignupForm> response2 = authService.signup(request);
        assertThat(response2.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }


    @Test
    void testSignupSuccess() {
        SignupRequest request = new SignupRequest();
        SignupForm registerForm = new SignupForm();
        registerForm.setId(UUID.randomUUID());


        ResponseEntity<SignupForm> response = authService.signup(request);

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

        try {
            authService.changeFormStatus(!successfully, email);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
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
