package com.wellness.ritmo.application.usecase.user;

import com.wellness.ritmo.api.dto.UserCreateDto;
import com.wellness.ritmo.api.dto.UserResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserMapper;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUseCasesTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private CreateUserUseCase createUserUseCase;

    @InjectMocks
    private GetUserUseCase getUserUseCase;

    @InjectMocks
    private ListUsersUseCase listUsersUseCase;

    private User user;
    private UserCreateDto createDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("johndoe");
        user.setEmail("john.doe@example.com");
        user.setPassword("$2a$10$hashedPassword");
        user.setCreatedOn(LocalDateTime.now());

        createDto = new UserCreateDto();
        createDto.setUserName("johndoe");
        createDto.setEmail("john.doe@example.com");
        createDto.setPassword("senha123");
    }

    @Test
    @DisplayName("CreateUserUseCase deve executar com sucesso e retornar UserResponseDto")
    void createUserUseCaseShouldExecuteSuccessfully() {
        when(userService.save("johndoe", "john.doe@example.com", "senha123"))
                .thenReturn(user);

        UserResponseDto result = createUserUseCase.execute(createDto);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("johndoe");
        assertThat(result.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("CreateUserUseCase deve extrair primitivos do DTO antes de chamar UserService")
    void createUserUseCaseShouldExtractPrimitives() {
        when(userService.save("johndoe", "john.doe@example.com", "senha123"))
                .thenReturn(user);

        createUserUseCase.execute(createDto);

        assertThat(createDto.getUserName()).isEqualTo("johndoe");
        assertThat(createDto.getEmail()).isEqualTo("john.doe@example.com");
        assertThat(createDto.getPassword()).isEqualTo("senha123");
    }

    @Test
    @DisplayName("GetUserUseCase deve retornar UserResponseDto quando usuário é encontrado")
    void getUserUseCaseShouldReturnUserWhenFound() {
        when(userService.searchById(1L)).thenReturn(user);

        UserResponseDto result = getUserUseCase.execute(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getUsername()).isEqualTo("johndoe");
    }

    @Test
    @DisplayName("GetUserUseCase deve lançar EntityNotFoundException quando usuário não é encontrado")
    void getUserUseCaseShouldThrowEntityNotFoundExceptionWhenUserNotFound() {
        Long userId = 999L;
        when(userService.searchById(userId))
                .thenThrow(new EntityNotFoundException("Usuário não encontrado: " + userId));

        assertThatThrownBy(() -> getUserUseCase.execute(userId))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("Usuário não encontrado: " + userId);
    }

    @Test
    @DisplayName("ListUsersUseCase deve retornar Page de UserResponseDto")
    void listUsersUseCaseShouldReturnPageOfUsers() {
        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setCreatedOn(LocalDateTime.now());

        List<User> users = List.of(user, user2);
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> userPage = new PageImpl<>(users, pageable, 2);

        when(userService.getAll(pageable)).thenReturn(userPage);

        Page<UserResponseDto> result = listUsersUseCase.execute(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getUsername()).isEqualTo("johndoe");
        assertThat(result.getContent().get(1).getUsername()).isEqualTo("user2");
    }

    @Test
    @DisplayName("ListUsersUseCase deve retornar página vazia quando não há usuários")
    void listUsersUseCaseShouldReturnEmptyPageWhenNoUsers() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(userService.getAll(pageable)).thenReturn(emptyPage);

        Page<UserResponseDto> result = listUsersUseCase.execute(pageable);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();
    }
}
