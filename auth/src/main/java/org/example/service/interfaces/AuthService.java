package org.example.service.interfaces;

import org.example.domain.SignupForm;
import org.example.domain.SignupRequest;
import org.example.domain.SignupResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface AuthService {

    /**
     * Регистрация в приложении.
     * Сохраняет форму в статусе ожидания.
     * Отправляет запрос на разрешение регистрации на внешний сервис через Kafka.
     * @param request - форма регистрации.
     * @return возврацает форму, успешность операции, id формы.
     */
    ResponseEntity<SignupResponse> signup(SignupRequest request);

    /**
     * Получение формы по id.
     * На случай, если не пришло уведоммление на почту.
     * @param id - id формы.
     * @return возвращает форму из БД.
     */

    ResponseEntity<SignupForm> getForm(UUID id);

    /**
     * Изменение статуса заявки после получения ответа от внешнего сервиса через kafka.
     * @param successfully - одобрили ли заявку.
     * @param email - поиск записи по email.
     */

    void changeFormStatus(Boolean successfully, String email);
}
