package com.wellness.ritmo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wellness.ritmo.api.dto.GeneratePlanRequest;
import com.wellness.ritmo.api.exception.GlobalExceptionHandler;
import com.wellness.ritmo.domain.model.*;
import com.wellness.ritmo.domain.model.Enum.GoalStatus;
import com.wellness.ritmo.domain.model.Enum.GoalType;
import com.wellness.ritmo.domain.model.Enum.SessionStatus;
import com.wellness.ritmo.domain.model.Enum.SessionType;
import com.wellness.ritmo.domain.service.TrainingPlanService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TrainingPlanController.class)
@Import(GlobalExceptionHandler.class)
class TrainingPlanControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TrainingPlanService trainingPlanService;

    private static final Long USER_ID = 1L;
    private static final Long GOAL_ID = 10L;
    private static final Long PLAN_ID = 100L;
    private static final Long SESSION_ID = 200L;
    private static final String BASE_URL = "/users/{userId}/training-plans";

    private TrainingPlan plan;
    private TrainingSession session;

    @BeforeEach
    void setUp() {
        User user = new User();
        user.setId(USER_ID);

        Goal goal = new Goal();
        goal.setId(GOAL_ID);
        goal.setUser(user);
        goal.setGoalType(GoalType.PACE);
        goal.setStatus(GoalStatus.OPEN);
        goal.setWeeklyFrequency(3);

        plan = new TrainingPlan();
        plan.setId(PLAN_ID);
        plan.setUser(user);
        plan.setGoal(goal);
        plan.setActive(true);
        plan.setWeekStart(LocalDate.now().with(DayOfWeek.MONDAY));
        plan.setCreatedAt(LocalDateTime.now());

        session = new TrainingSession();
        session.setId(SESSION_ID);
        session.setTrainingPlan(plan);
        session.setSessionType(SessionType.EASY);
        session.setTargetPaceSec(360);
        session.setDayOfWeek(DayOfWeek.TUESDAY);
        session.setStatus(SessionStatus.PENDING);

        TrainingSession session2 = new TrainingSession();
        session2.setId(201L);
        session2.setTrainingPlan(plan);
        session2.setSessionType(SessionType.LONG_RUN);
        session2.setTargetPaceSec(330);
        session2.setDayOfWeek(DayOfWeek.SATURDAY);
        session2.setStatus(SessionStatus.PENDING);

        TrainingSession session3 = new TrainingSession();
        session3.setId(202L);
        session3.setTrainingPlan(plan);
        session3.setSessionType(SessionType.INTERVALS);
        session3.setTargetPaceSec(270);
        session3.setDayOfWeek(DayOfWeek.THURSDAY);
        session3.setStatus(SessionStatus.PENDING);

        plan.setSessions(List.of(session, session2, session3));
    }

    @Test
    @DisplayName("POST - 201: Gera plano com sucesso")
    void shouldReturn201WhenPlanGenerated() throws Exception {
        when(trainingPlanService.generatePlan(USER_ID, GOAL_ID)).thenReturn(plan);

        mockMvc.perform(post(BASE_URL, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GeneratePlanRequest(GOAL_ID))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.week").isNumber())
                .andExpect(jsonPath("$.startsOn").exists())
                .andExpect(jsonPath("$.sessions", hasSize(3)))
                .andExpect(jsonPath("$.sessions[0].pace").isString());

        verify(trainingPlanService).generatePlan(USER_ID, GOAL_ID);
    }

    @Test
    @DisplayName("POST - 409: Já existe plano ativo")
    void shouldReturn409WhenActivePlanExists() throws Exception {
        when(trainingPlanService.generatePlan(USER_ID, GOAL_ID))
                .thenThrow(new IllegalStateException("Usuário " + USER_ID + " já possui um plano ativo"));

        mockMvc.perform(post(BASE_URL, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GeneratePlanRequest(GOAL_ID))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Usuário " + USER_ID + " já possui um plano ativo"));
    }

    @Test
    @DisplayName("POST - 404: Goal não encontrado")
    void shouldReturn404WhenGoalNotFound() throws Exception {
        when(trainingPlanService.generatePlan(USER_ID, GOAL_ID))
                .thenThrow(new EntityNotFoundException("Goal " + GOAL_ID + " não encontrado para o usuário " + USER_ID));

        mockMvc.perform(post(BASE_URL, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new GeneratePlanRequest(GOAL_ID))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Goal " + GOAL_ID + " não encontrado para o usuário " + USER_ID));
    }

    @Test
    @DisplayName("GET /current - 200: Retorna plano ativo")
    void shouldReturn200WhenCurrentPlanFound() throws Exception {
        when(trainingPlanService.findCurrentPlan(USER_ID)).thenReturn(plan);

        mockMvc.perform(get(BASE_URL + "/current", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.week").isNumber())
                .andExpect(jsonPath("$.startsOn").exists())
                .andExpect(jsonPath("$.sessions", hasSize(3)));
    }

    @Test
    @DisplayName("GET /current - 404: Nenhum plano ativo")
    void shouldReturn404WhenNoCurrentPlan() throws Exception {
        when(trainingPlanService.findCurrentPlan(USER_ID))
                .thenThrow(new EntityNotFoundException("Nenhum plano ativo encontrado para o usuário " + USER_ID));

        mockMvc.perform(get(BASE_URL + "/current", USER_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("Nenhum plano ativo encontrado para o usuário " + USER_ID));
    }

    @Test
    @DisplayName("GET /{planId}/sessions - 200: Retorna sessões do plano")
    void shouldReturn200WithSessions() throws Exception {
        when(trainingPlanService.findSessions(USER_ID, PLAN_ID)).thenReturn(plan.getSessions());

        mockMvc.perform(get(BASE_URL + "/{planId}/sessions", USER_ID, PLAN_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].sessionType").exists())
                .andExpect(jsonPath("$[0].pace").isString());
    }

    @Test
    @DisplayName("GET /{planId}/sessions - 404: Plano não encontrado")
    void shouldReturn404WhenPlanNotFound() throws Exception {
        when(trainingPlanService.findSessions(USER_ID, PLAN_ID))
                .thenThrow(new EntityNotFoundException("Plano " + PLAN_ID + " não encontrado para o usuário " + USER_ID));

        mockMvc.perform(get(BASE_URL + "/{planId}/sessions", USER_ID, PLAN_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH complete - 200: Sessão marcada como concluída")
    void shouldReturn200WhenSessionCompleted() throws Exception {
        session.setStatus(SessionStatus.COMPLETED);
        session.setCompletedAt(LocalDateTime.now());
        when(trainingPlanService.completeSession(USER_ID, PLAN_ID, SESSION_ID)).thenReturn(session);

        mockMvc.perform(patch(BASE_URL + "/{planId}/sessions/{sessionId}/complete", USER_ID, PLAN_ID, SESSION_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SESSION_ID))
                .andExpect(jsonPath("$.pace").value("06:00"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.completedAt").exists());
    }

    @Test
    @DisplayName("PATCH complete - 404: Sessão não encontrada")
    void shouldReturn404WhenSessionNotFound() throws Exception {
        when(trainingPlanService.completeSession(USER_ID, PLAN_ID, SESSION_ID))
                .thenThrow(new EntityNotFoundException(
                        "Sessão " + SESSION_ID + " não encontrada para o plano " + PLAN_ID + " do usuário " + USER_ID));

        mockMvc.perform(patch(BASE_URL + "/{planId}/sessions/{sessionId}/complete", USER_ID, PLAN_ID, SESSION_ID))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @DisplayName("PATCH complete - 409: Sessão já concluída")
    void shouldReturn409WhenSessionAlreadyCompleted() throws Exception {
        when(trainingPlanService.completeSession(USER_ID, PLAN_ID, SESSION_ID))
                .thenThrow(new IllegalStateException("Sessão " + SESSION_ID + " já está concluída"));

        mockMvc.perform(patch(BASE_URL + "/{planId}/sessions/{sessionId}/complete", USER_ID, PLAN_ID, SESSION_ID))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Sessão " + SESSION_ID + " já está concluída"));
    }
}
