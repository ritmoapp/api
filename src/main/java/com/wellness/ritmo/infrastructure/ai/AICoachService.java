package com.wellness.ritmo.infrastructure.ai;

import com.wellness.ritmo.domain.model.AIFeedback;
import com.wellness.ritmo.domain.model.Activity;
import com.wellness.ritmo.domain.model.UserProfileHistory;
import com.wellness.ritmo.domain.repository.AIFeedbackRepository;
import com.wellness.ritmo.domain.repository.UserProfileHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AICoachService {

    private final ChatClient chatClient;
    private final AIFeedbackRepository feedbackRepository;
    private final UserProfileHistoryRepository profileHistoryRepository;

    @Value("${spring.ai.ollama.chat.model}")
    private String modelUsed;

    @Async("aiTaskExecutor")
    @Transactional
    public void generateFeedbackAsync(Activity activity) {
        log.info("[AI Coach] Gerando feedback para activity id={}", activity.getId());
        try {
            String prompt = buildPrompt(activity);
            String response = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            AIFeedback feedback = new AIFeedback();
            feedback.setActivity(activity);
            feedback.setFeedbackText(response);
            feedback.setModelUsed(modelUsed);
            feedbackRepository.save(feedback);

            log.info("[AI Coach] Feedback salvo para activity id={}", activity.getId());
        } catch (Exception e) {
            log.error("[AI Coach] Falha ao gerar feedback para activity id={}: {}", activity.getId(), e.getMessage());
        }
    }

    private String buildPrompt(Activity activity) {
        List<UserProfileHistory> history = profileHistoryRepository
                .findTop3ByUserIdOrderByRecordedAtDesc(activity.getUser().getId());

        String historyBlock = history.isEmpty()
                ? "Sem histórico biométrico disponível."
                : history.stream()
                        .map(h -> String.format(
                                "- Data: %s | Peso: %.1f kg | Pace médio: %d seg/km | Condicionamento: %s",
                                h.getRecordedAt().toLocalDate(),
                                h.getWeightKg() != null ? h.getWeightKg() : 0.0,
                                h.getPaceAvgSeg() != null ? h.getPaceAvgSeg() : 0,
                                h.getConditioningLevel() != null ? h.getConditioningLevel() : "N/A"
                        ))
                        .reduce("", (a, b) -> a + "\n" + b);

        return """
                Você é um treinador de corrida técnico e motivador. Analise o treino abaixo com base no histórico biométrico do atleta.
                Seja direto, específico e motivador. Limite sua resposta a 4 parágrafos.

                === TREINO REALIZADO ===
                Distância: %.2f km
                Duração: %d minutos e %d segundos
                Pace médio: %d seg/km (%.0f min/km)
                Frequência cardíaca média: %s bpm
                Frequência cardíaca máxima: %s bpm
                Esforço percebido (escala Borg 6-20): %s

                === HISTÓRICO BIOMÉTRICO (últimas 3 entradas) ===
                %s

                === INSTRUÇÕES ===
                1. Avalie a qualidade do treino (pace, duração, esforço)
                2. Com base no histórico de peso e condicionamento, identifique risco de lesão ou fadiga acumulada
                3. Sugira ajuste de carga para o próximo treino
                4. Encerre com uma frase motivacional personalizada
                """.formatted(
                activity.getDistanceKm().doubleValue(),
                activity.getDurationSec() / 60,
                activity.getDurationSec() % 60,
                activity.getPaceAvgSec() != null ? activity.getPaceAvgSec() : 0,
                activity.getPaceAvgSec() != null ? activity.getPaceAvgSec() / 60.0 : 0.0,
                activity.getHeartRateAvg() != null ? activity.getHeartRateAvg() : "não informado",
                activity.getHeartRateMax() != null ? activity.getHeartRateMax() : "não informado",
                activity.getPerceivedEffort() != null ? activity.getPerceivedEffort() : "não informado",
                historyBlock
        );
    }
}
