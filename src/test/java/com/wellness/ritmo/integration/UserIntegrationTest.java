package com.wellness.ritmo.integration;

import com.wellness.ritmo.util.IntegrationTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Phase 1 - User Integration Tests")
class UserIntegrationTest extends IntegrationTestBase {

    // ======================== POST /users ========================

    @Test
    @DisplayName("POST /users - deve criar usuário com sucesso e retornar 201")
    void shouldCreateUserSuccessfully() throws Exception {
        var dto = validUserCreateDto("newuser", "new@test.com");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.username").value("newuser"))
                .andExpect(jsonPath("$.email").value("new@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        assertTrue(userRepository.existsByUsername("newuser"));
        assertTrue(userRepository.existsByEmail("new@test.com"));
    }

    @Test
    @DisplayName("POST /users - deve retornar 409 quando username já existe")
    void shouldReturn409WhenUsernameAlreadyExists() throws Exception {
        createUser("existinguser", "existing@test.com");

        var dto = validUserCreateDto("existinguser", "other@test.com");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /users - deve retornar 409 quando email já está cadastrado")
    void shouldReturn409WhenEmailAlreadyExists() throws Exception {
        createUser("existinguser", "existing@test.com");

        var dto = validUserCreateDto("differentuser", "existing@test.com");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /users - deve retornar 422 para dados inválidos (email inválido, username vazio, senha curta)")
    void shouldReturn422ForInvalidData() throws Exception {
        var dto = validUserCreateDto("", "invalid-email");
        dto.setPassword("123");

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());
    }

    // ======================== GET /users/{id} ========================

    @Test
    @WithMockUser
    @DisplayName("GET /users/{id} - deve retornar usuário quando encontrado (200)")
    void shouldReturnUserById() throws Exception {
        var user = createUser("getuser", "getuser@test.com");

        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()))
                .andExpect(jsonPath("$.username").value("getuser"))
                .andExpect(jsonPath("$.email").value("getuser@test.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /users/{id} - deve retornar 404 para id inexistente")
    void shouldReturn404WhenUserNotFound() throws Exception {
        mockMvc.perform(get("/users/999999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /users/{id} - endpoint acessível sem autenticação (anyRequest().permitAll() configurado)")
    void shouldBeAccessibleWithoutAuthentication() throws Exception {
        // Com a configuração atual de segurança (permitAll()), este endpoint
        // não exige autenticação. Uma requisição anônima retorna 200 se o usuário existir.
        var user = createUser("publicuser", "publicuser@test.com");

        mockMvc.perform(get("/users/" + user.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(user.getId()));
    }

    // ======================== GET /users ========================

    @Test
    @WithMockUser
    @DisplayName("GET /users - deve retornar lista paginada com totalElements >= 3")
    void shouldReturnPagedUsers() throws Exception {
        createUser("listuser1", "listuser1@test.com");
        createUser("listuser2", "listuser2@test.com");
        createUser("listuser3", "listuser3@test.com");

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.content[0].id").exists())
                .andExpect(jsonPath("$.content[0].password").doesNotExist());
    }

    @Test
    @WithMockUser
    @DisplayName("GET /users - paginação: ?page=0&size=2 deve retornar no máximo 2 elementos")
    void shouldReturnPaginatedResults() throws Exception {
        createUser("pageuser1", "pageuser1@test.com");
        createUser("pageuser2", "pageuser2@test.com");
        createUser("pageuser3", "pageuser3@test.com");

        mockMvc.perform(get("/users")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.number").value(0))
                .andExpect(jsonPath("$.totalElements").value(greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.totalPages").value(greaterThanOrEqualTo(2)));
    }

    @Test
    @WithMockUser
    @DisplayName("GET /users - deve retornar lista vazia quando não há usuários (200)")
    void shouldReturnEmptyListWhenNoUsers() throws Exception {
        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));
    }
}
