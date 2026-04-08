package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.ActivityRequestDto;
import com.wellness.ritmo.api.dto.ActivityResponseDto;
import com.wellness.ritmo.domain.service.ActivityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/{userId}/activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Registro e consulta de treinos realizados")
public class ActivityController {

    private final ActivityService activityService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Registrar treino concluído",
            description = "Salva a activity, avalia meta associada e dispara análise IA em background"
    )
    public ActivityResponseDto register(
            @PathVariable Long userId,
            @Valid @RequestBody ActivityRequestDto dto
    ) {
        return activityService.register(userId, dto);
    }
}
