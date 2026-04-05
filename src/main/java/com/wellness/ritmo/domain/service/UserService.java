package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.api.dto.UserCreateDto;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserService {
    private final UserRepository userRepository;

    public User save(UserCreateDto newUser) {
        User user = new User();
        user.setCreatedOn(LocalDateTime.now());
        user.setUsername(newUser.getUserName());
        user.setPassword(newUser.getPassword());
        user.setEmail(newUser.getEmail());

        return userRepository.save(user);
    }

    public User searchById(Long id) {
        return userRepository.findById(id).orElseThrow();
    }

    @Transactional(readOnly = true)
    public List<User> getAll() {
        return userRepository.findAll();
    }
}
