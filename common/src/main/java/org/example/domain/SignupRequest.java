package org.example.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SignupRequest {
    @NotBlank(message = "Логин не может быть пустым")
    @Size(min = 5, message = "Логин должен содержать не менее 5 символов")
    @Pattern(regexp = "^[a-zA-Z]+$", message = "Логин может содержать только латинские буквы")
    private String login;

    @NotBlank(message = "Пароль не может быть пустым")
    @Size(min = 5, message = "Пароль должен содержать не менее 5 символов")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z]).*$", message = "Пароль должен содержать хотя бы одну заглавную и одну строчную букву")
    private String password;

    @Email(message = "Введите действительный адрес электронной почты")
    @NotBlank(message = "Адрес электронной почты обязателен")
    private String email;

    @NotBlank(message = "Полное имя обязательно")
    @Pattern(regexp = "^[а-яА-Яa-zA-Z]+(?:\\s+[а-яА-Яa-zA-Z]+){2}$", message = "Полное имя должно состоять из трех слов")
    private String fullName;
}
