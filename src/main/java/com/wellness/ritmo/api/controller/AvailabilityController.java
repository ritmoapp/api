package com.wellness.ritmo.api.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/{userId}/activities")
@RequiredArgsConstructor
@Tag(name = "Activities", description = "Registro e consulta de treinos realizados")
public class AvailabilityController {
}
