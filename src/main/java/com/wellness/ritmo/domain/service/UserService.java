package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.api.exception.UserAlreadyExistsException;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional
    public User save(String username, String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException(
                    "Email já cadastrado: " + email
            );
        }
        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyExistsException(
                    "Username já cadastrado: " + username
            );
        }

        User user = new User();
        user.setCreatedOn(LocalDateTime.now());
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);

        log.info("[UserService] Criando novo usuário: {}", username);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User searchById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Page<User> getAll(Pageable pageable) {
        return userRepository.findAll(pageable);
    }
}
