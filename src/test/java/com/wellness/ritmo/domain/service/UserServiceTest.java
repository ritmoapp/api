package com.wellness.ritmo.domain.service;

import com.wellness.ritmo.api.dto.UserCreateDto;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private UserCreateDto userCreateDto;
    private User savedUser;

    @BeforeEach
    void setUp() {
        userCreateDto = new UserCreateDto();
        userCreateDto.setUserName("johndoe");
        userCreateDto.setEmail("john.doe@example.com");
        userCreateDto.setPassword("senha123");

        savedUser = new User();
        savedUser.setId(1L);
        savedUser.setUsername("johndoe");
        savedUser.setEmail("john.doe@example.com");
        savedUser.setPassword("$2a$10$hashedPassword");
        savedUser.setCreatedOn(LocalDateTime.now());
    }

    @Test
    @DisplayName("deve salvar usuário com senha criptografada usando BCrypt")
    void shouldSaveUserWithEncryptedPassword() {
        String rawPassword = "senha123";
        String encodedPassword = "$2a$10$hashedPassword";

        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        User result = userService.save(userCreateDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());
        
        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPassword()).isEqualTo(encodedPassword);
        assertThat(capturedUser.getCreatedOn()).isNotNull();
        
        verify(passwordEncoder).encode(rawPassword);
    }

    @Test
    @DisplayName("deve lançar EntityNotFoundException quando usuário não for encontrado por ID")
    void shouldThrowEntityNotFoundExceptionWhenUserNotFoundById() {
        Long userId = 999L;
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.searchById(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Usuário não encontrado: " + userId);

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("deve retornar usuário quando encontrado por ID")
    void shouldReturnUserWhenFoundById() {
        Long userId = 1L;
        when(userRepository.findById(userId)).thenReturn(Optional.of(savedUser));

        User result = userService.searchById(userId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(userId);
        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");

        verify(userRepository).findById(userId);
    }

    @Test
    @DisplayName("deve retornar página de usuários com paginação")
    void shouldReturnPagedUsers() {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");

        List<User> users = List.of(user1, user2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> expectedPage = new PageImpl<>(users, pageable, users.size());

        when(userRepository.findAll(pageable)).thenReturn(expectedPage);

        Page<User> result = userService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(User::getUsername)
                .containsExactly("user1", "user2");
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getNumber()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(10);

        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("deve retornar página vazia quando não houver usuários")
    void shouldReturnEmptyPageWhenNoUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userRepository.findAll(pageable)).thenReturn(emptyPage);

        Page<User> result = userService.getAll(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(userRepository).findAll(pageable);
    }

    @Test
    @DisplayName("deve garantir que a senha seja sempre criptografada antes de salvar")
    void shouldAlwaysEncryptPasswordBeforeSaving() {
        String rawPassword = "mySecretPass";
        String hashedPassword = "$2a$10$differentHash";
        
        userCreateDto.setPassword(rawPassword);

        when(passwordEncoder.encode(rawPassword)).thenReturn(hashedPassword);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.save(userCreateDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getPassword()).isNotEqualTo(rawPassword);
        assertThat(capturedUser.getPassword()).isEqualTo(hashedPassword);
        
        verify(passwordEncoder, times(1)).encode(rawPassword);
    }

    @Test
    @DisplayName("deve definir createdOn automaticamente ao salvar usuário")
    void shouldSetCreatedOnAutomaticallyWhenSaving() {
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        LocalDateTime beforeSave = LocalDateTime.now();
        userService.save(userCreateDto);
        LocalDateTime afterSave = LocalDateTime.now();

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getCreatedOn()).isNotNull();
        assertThat(capturedUser.getCreatedOn()).isBetween(beforeSave, afterSave);
    }

    @Test
    @DisplayName("deve preservar todos os campos do DTO ao salvar")
    void shouldPreserveAllDtoFieldsWhenSaving() {
        when(passwordEncoder.encode(anyString())).thenReturn("$2a$10$hash");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        userService.save(userCreateDto);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getUsername()).isEqualTo(userCreateDto.getUserName());
        assertThat(capturedUser.getEmail()).isEqualTo(userCreateDto.getEmail());
    }
}
