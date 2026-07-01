package com.wellness.ritmo.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellness.ritmo.api.dto.OnboardingDto;
import com.wellness.ritmo.api.dto.UserCreateDto;
import com.wellness.ritmo.domain.model.Enum.ConditioningLevel;
import com.wellness.ritmo.domain.model.Enum.Gender;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.repository.UserProfileHistoryRepository;
import com.wellness.ritmo.domain.repository.UserProfileRepository;
import com.wellness.ritmo.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@Transactional
public abstract class IntegrationTestBase {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected UserProfileRepository userProfileRepository;

    @Autowired
    protected UserProfileHistoryRepository userProfileHistoryRepository;

    @Autowired
    protected BCryptPasswordEncoder passwordEncoder;

    @MockitoBean
    protected ChatClient chatClient;

    @BeforeEach
    void cleanDatabase() {
        userProfileHistoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    protected User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode("senha123"));
        user.setCreatedOn(LocalDateTime.now());
        return userRepository.save(user);
    }

    protected OnboardingDto validOnboardingDto() {
        return new OnboardingDto(
            Gender.MALE,
            LocalDate.of(1990, 5, 15),
            175,
            70.0,
            ConditioningLevel.INTERMEDIATE,
            300,
            30.0
        );
    }

    protected UserCreateDto validUserCreateDto(String username, String email) {
        return new UserCreateDto(username, email, "senha123");
    }
}
