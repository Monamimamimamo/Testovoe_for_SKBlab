package org.example.domain.repo;

import org.example.domain.SignupForm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RegisterFormRepo extends JpaRepository<SignupForm, UUID> {
    Optional<SignupForm> findByEmail(String email);
}
