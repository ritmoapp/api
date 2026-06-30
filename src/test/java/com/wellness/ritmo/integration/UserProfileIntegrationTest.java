package com.wellness.ritmo.integration;

import com.wellness.ritmo.api.dto.UserProfileUpdateDto;
import com.wellness.ritmo.domain.model.Enum.ConditioningLevel;
import com.wellness.ritmo.domain.model.Enum.Gender;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.model.UserProfile;
import com.wellness.ritmo.util.IntegrationTestBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("Phase 0 - UserProfile Integration Tests")
class UserProfileIntegrationTest extends IntegrationTestBase {

    private static final String USERNAME = "profileuser";
    private static final String EMAIL = "profileuser@test.com";

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = createUser(USERNAME, EMAIL);
    }

    // ======================== POST /profile ========================

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("POST /profile - deve criar perfil com sucesso e retornar 201")
    void shouldCreateInitialProfileSuccessfully() throws Exception {
        mockMvc.perform(post("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOnboardingDto())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.weightKg").value(70.0))
                .andExpect(jsonPath("$.heightCm").value(175))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.conditioningLevel").value("INTERMEDIATE"))
                .andExpect(jsonPath("$.userId").value(testUser.getId()));

        assertTrue(userProfileRepository.existsByUserId(testUser.getId()));
    }

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("POST /profile - deve retornar 409 quando usuário já possui perfil")
    void shouldReturn409WhenProfileAlreadyExists() throws Exception {
        mockMvc.perform(post("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOnboardingDto())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOnboardingDto())))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("POST /profile - deve retornar 422 para dados inválidos (weightKg abaixo do mínimo)")
    void shouldReturn422WhenOnboardingDataIsInvalid() throws Exception {
        var dto = validOnboardingDto();
        dto.setWeightKg(-10.0);

        mockMvc.perform(post("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    @DisplayName("POST /profile - deve retornar 4xx sem autenticação válida")
    void shouldReturn4xxWithoutValidAuthentication() throws Exception {
        // Com anyRequest().permitAll(), o usuário anônimo passa pelo Spring Security,
        // mas AuthenticatedUserService não encontra "anonymousUser" no DB → 404
        mockMvc.perform(post("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validOnboardingDto())))
                .andExpect(status().is4xxClientError());
    }

    // ======================== PUT /profile ========================

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("PUT /profile - deve atualizar campos mutáveis com sucesso e retornar 200")
    void shouldUpdateProfileSuccessfully() throws Exception {
        saveProfile(testUser, 70.0, ConditioningLevel.INTERMEDIATE, 300);

        var updateDto = new UserProfileUpdateDto();
        updateDto.setWeightKg(75.0);
        updateDto.setConditioningLevel(ConditioningLevel.ADVANCED);
        updateDto.setPaceAvgSeg(320);
        updateDto.setWeeklyMileageKm(40.0);

        mockMvc.perform(put("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(75.0))
                .andExpect(jsonPath("$.conditioningLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.paceAvgSeg").value(320))
                .andExpect(jsonPath("$.weeklyMileageKm").value(40.0))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.heightCm").value(175))
                .andExpect(jsonPath("$.birthDate").value("1990-05-15"));
    }

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("PUT /profile - campos imutáveis (gender, birthDate, heightCm) devem ser preservados")
    void shouldPreserveImmutableFieldsOnUpdate() throws Exception {
        saveProfile(testUser, 70.0, ConditioningLevel.INTERMEDIATE, 300);

        var updateDto = new UserProfileUpdateDto();
        updateDto.setWeightKg(80.0);

        mockMvc.perform(put("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(80.0))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.birthDate").value("1990-05-15"))
                .andExpect(jsonPath("$.heightCm").value(175));
    }

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("PUT /profile - deve retornar 404 quando perfil não existe")
    void shouldReturn404WhenProfileNotFoundOnUpdate() throws Exception {
        var updateDto = new UserProfileUpdateDto();
        updateDto.setWeightKg(75.0);

        mockMvc.perform(put("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("PUT /profile - partial update deve alterar apenas campos fornecidos, preservando os demais")
    void shouldUpdateOnlyProvidedFields() throws Exception {
        saveProfile(testUser, 70.0, ConditioningLevel.INTERMEDIATE, 300);

        var partialUpdate = new UserProfileUpdateDto();
        partialUpdate.setWeightKg(78.5);

        mockMvc.perform(put("/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weightKg").value(78.5))
                .andExpect(jsonPath("$.conditioningLevel").value("INTERMEDIATE"))
                .andExpect(jsonPath("$.paceAvgSeg").value(300))
                .andExpect(jsonPath("$.weeklyMileageKm").value(30.0));
    }

    // ======================== GET /profile ========================

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("GET /profile - deve retornar perfil completo com todos os campos (200)")
    void shouldReturnProfileSuccessfully() throws Exception {
        saveProfile(testUser, 70.0, ConditioningLevel.INTERMEDIATE, 300);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.gender").value("MALE"))
                .andExpect(jsonPath("$.birthDate").value("1990-05-15"))
                .andExpect(jsonPath("$.heightCm").value(175))
                .andExpect(jsonPath("$.weightKg").value(70.0))
                .andExpect(jsonPath("$.conditioningLevel").value("INTERMEDIATE"))
                .andExpect(jsonPath("$.paceAvgSeg").value(300))
                .andExpect(jsonPath("$.weeklyMileageKm").value(30.0))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("GET /profile - deve retornar 404 quando usuário não possui perfil")
    void shouldReturn404WhenProfileNotFound() throws Exception {
        mockMvc.perform(get("/profile"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = USERNAME)
    @DisplayName("GET /profile - isolamento: usuário A não deve ver perfil de usuário B")
    void shouldReturnOnlyAuthenticatedUserProfile() throws Exception {
        saveProfile(testUser, 70.0, ConditioningLevel.INTERMEDIATE, 300);

        User userB = createUser("otheruserb", "otheruserb@test.com");
        saveProfile(userB, 55.0, ConditioningLevel.BEGINNER, 420);

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(testUser.getId()))
                .andExpect(jsonPath("$.weightKg").value(70.0));
    }

    // ======================== Helpers ========================

    private void saveProfile(User user, Double weightKg, ConditioningLevel level, Integer pace) {
        UserProfile profile = new UserProfile();
        profile.setUser(user);
        profile.setGender(Gender.MALE);
        profile.setBirthDate(LocalDate.of(1990, 5, 15));
        profile.setHeightCm(175);
        profile.setWeightKg(weightKg);
        profile.setConditioningLevel(level);
        profile.setPaceAvgSeg(pace);
        profile.setWeeklyMileageKm(30.0);
        userProfileRepository.save(profile);
    }
}
