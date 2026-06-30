package com.wellness.ritmo.api.dto;

import com.wellness.ritmo.domain.model.Enum.ConditioningLevel;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * DTO para atualização de perfil de usuário.
 * 
 * Diferentemente de {@link OnboardingDto}, este DTO contém apenas os campos
 * que podem ser atualizados APÓS a criação inicial do perfil.
 * 
 * Campos EXCLUÍDOS (imutáveis após onboarding):
 * - gender: Determinado durante onboarding, não muda
 * - birthDate: Usado para cálculos de idade, imutável para auditoria
 * - heightCm: Baseline biométrico, imutável para métricas históricas
 * 
 * Campos INCLUSOS (frequentemente atualizados):
 * - weightKg: Rastreado ao longo do treinamento (progresso)
 * - conditioningLevel: Reavaliado periodicamente com base em desempenho
 * - paceAvgSeg: Muda conforme o treinamento evolui
 * - weeklyMileageKm: Varia com o volume de treinamento
 * 
 * Padrão: Todos os campos são OPCIONAIS. Se um campo não for fornecido (null),
 * a operação de update o ignora (não sobrescreve).
 * 
 * Exemplo de uso:
 * <pre>
 * PUT /users/123/profile
 * {
 *   "weightKg": 65.0,
 *   "paceAvgSeg": 280
 * }
 * </pre>
 * 
 * Resultado: Apenas weightKg e paceAvgSeg são atualizados.
 * Os outros campos permanecem inalterados.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserProfileUpdateDto {

    /**
     * Peso corporal em quilogramas.
     * 
     * Não é validado no update porque o usuário pode estar em diferentes
     * fases do treinamento e períodos do ano.
     */
    @DecimalMin(value = "30.0", message = "Peso deve ser no mínimo 30 kg")
    @DecimalMax(value = "300.0", message = "Peso deve ser no máximo 300 kg")
    private Double weightKg;

    /**
     * Nível de condicionamento físico.
     * 
     * Pode ser reavaliado durante o treinamento conforme a evolução
     * do atleta.
     */
    private ConditioningLevel conditioningLevel;

    /**
     * Pace médio em segundos por km.
     * 
     * Muda ao longo do treinamento. A validação de compatibilidade
     * com nível de condicionamento é feita no serviço de domínio.
     */
    @Min(value = 150, message = "Pace inválido: muito rápido (mínimo 2:30/km)")
    @Max(value = 600, message = "Pace inválido: muito lento (máximo 10:00/km)")
    private Integer paceAvgSeg;

    /**
     * Quilometragem semanal em quilômetros.
     * 
     * Varia conforme o volume de treinamento planejado.
     */
    @DecimalMin(value = "0.0", message = "Quilometragem semanal não pode ser negativa")
    @DecimalMax(value = "300.0", message = "Quilometragem semanal muito alta")
    private Double weeklyMileageKm;
}
