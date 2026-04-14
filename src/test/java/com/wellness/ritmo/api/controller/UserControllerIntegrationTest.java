package com.wellness.ritmo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellness.ritmo.api.dto.UserCreateDto;
import com.wellness.ritmo.api.dto.UserResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserMapper;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    private User user;
    private UserCreateDto userCreateDto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("johndoe");
        user.setEmail("john.doe@example.com");
        user.setPassword("$2a$10$hashedPassword");
        user.setCreatedOn(LocalDateTime.of(2026, 4, 14, 10, 30));

        userCreateDto = new UserCreateDto();
        userCreateDto.setUserName("johndoe");
        userCreateDto.setEmail("john.doe@example.com");
        userCreateDto.setPassword("senha123");
    }

    @Test
    @DisplayName("POST /api/v1/user - deve criar usuário com sucesso e retornar 201")
    void shouldCreateUserSuccessfully() throws Exception {
        when(userService.save(any(UserCreateDto.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.createdOn").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/v1/user - deve retornar 422 quando userName está em branco")
    void shouldReturn422WhenUserNameIsBlank() throws Exception {
        userCreateDto.setUserName("");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/v1/user - deve retornar 422 quando email é inválido")
    void shouldReturn422WhenEmailIsInvalid() throws Exception {
        userCreateDto.setEmail("invalid-email");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/v1/user - deve retornar 422 quando password tem menos de 6 caracteres")
    void shouldReturn422WhenPasswordIsTooShort() throws Exception {
        userCreateDto.setPassword("12345");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/v1/user - deve retornar 422 quando password tem mais de 10 caracteres")
    void shouldReturn422WhenPasswordIsTooLong() throws Exception {
        userCreateDto.setPassword("12345678901");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/v1/user - deve retornar 422 quando campos obrigatórios estão ausentes")
    void shouldReturn422WhenRequiredFieldsAreMissing() throws Exception {
        UserCreateDto invalidDto = new UserCreateDto();

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("GET /api/v1/user/{id} - deve retornar usuário quando encontrado")
    void shouldReturnUserWhenFound() throws Exception {
        when(userService.searchById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.username").value("johndoe"))
                .andExpect(jsonPath("$.email").value("john.doe@example.com"))
                .andExpect(jsonPath("$.createdOn").exists())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("GET /api/v1/user/{id} - deve retornar 404 quando usuário não for encontrado")
    void shouldReturn404WhenUserNotFound() throws Exception {
        Long userId = 999L;
        when(userService.searchById(userId))
                .thenThrow(new EntityNotFoundException("Usuário não encontrado: " + userId));

        mockMvc.perform(get("/api/v1/user/" + userId))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/user - deve retornar página de usuários com paginação padrão")
    void shouldReturnPagedUsersWithDefaultPagination() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setCreatedOn(LocalDateTime.now());

        User user2 = new User();
        user2.setId(2L);
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setCreatedOn(LocalDateTime.now());

        List<User> users = List.of(user1, user2);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(0, 10), 2);

        when(userService.getAll(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].username").value("user1"))
                .andExpect(jsonPath("$.content[1].username").value("user2"))
                .andExpect(jsonPath("$.pageable").exists())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(1))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.number").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/user - deve retornar página customizada quando parâmetros são fornecidos")
    void shouldReturnCustomPageWhenParametersProvided() throws Exception {
        User user1 = new User();
        user1.setId(1L);
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setCreatedOn(LocalDateTime.now());

        List<User> users = List.of(user1);
        Page<User> userPage = new PageImpl<>(users, PageRequest.of(1, 5), 10);

        when(userService.getAll(any(Pageable.class))).thenReturn(userPage);

        mockMvc.perform(get("/api/v1/user")
                        .param("page", "1")
                        .param("size", "5")
                        .param("sort", "username,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.number").value(1))
                .andExpect(jsonPath("$.totalElements").value(10))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/user - deve retornar página vazia quando não há usuários")
    void shouldReturnEmptyPageWhenNoUsers() throws Exception {
        Page<User> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 10), 0);

        when(userService.getAll(any(Pageable.class))).thenReturn(emptyPage);

        mockMvc.perform(get("/api/v1/user"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0))
                .andExpect(jsonPath("$.totalPages").value(0));
    }

    @Test
    @DisplayName("POST /api/v1/user - deve validar que senha não contém apenas espaços")
    void shouldValidatePasswordNotOnlySpaces() throws Exception {
        userCreateDto.setPassword("      ");

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /api/v1/user - deve aceitar senha válida com 6 caracteres (limite mínimo)")
    void shouldAcceptPasswordWithMinimumLength() throws Exception {
        userCreateDto.setPassword("123456");
        when(userService.save(any(UserCreateDto.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/v1/user - deve aceitar senha válida com 10 caracteres (limite máximo)")
    void shouldAcceptPasswordWithMaximumLength() throws Exception {
        userCreateDto.setPassword("1234567890");
        when(userService.save(any(UserCreateDto.class))).thenReturn(user);

        mockMvc.perform(post("/api/v1/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userCreateDto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/v1/user - deve garantir que senha não é exposta na resposta")
    void shouldNotExposePasswordInResponse() throws Exception {
        when(userService.searchById(1L)).thenReturn(user);

        mockMvc.perform(get("/api/v1/user/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }
}
