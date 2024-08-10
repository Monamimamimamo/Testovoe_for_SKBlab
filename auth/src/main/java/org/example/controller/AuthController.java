package org.example.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.domain.SignupForm;
import org.example.domain.SignupRequest;
import org.example.domain.SignupResponse;
import org.example.service.AuthServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.UUID;

@RestController
@Slf4j
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthServiceImpl authService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody @Valid SignupRequest request){
        log.info("Принята форма: " + request);
        return authService.signup(request);
    }

    @GetMapping("/signup")
    public ResponseEntity<SignupForm> getForm(@RequestParam @Valid UUID id){
        log.info("Поиск формы по id: " + id);
        return authService.getForm(id);
    }
}
