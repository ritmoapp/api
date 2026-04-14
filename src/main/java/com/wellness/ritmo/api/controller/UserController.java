package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.UserCreateDto;
import com.wellness.ritmo.api.dto.UserResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserMapper;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springdoc.api.ErrorMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "created a new user", responses = {
            @ApiResponse(responseCode = "201", description = "recursos criado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "409", description = "Usuario email já cadastrado no sisterma", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class))),
            @ApiResponse(responseCode = "422", description = "Recursos não processado por dados de entrada invalidos", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
    })
    @PostMapping
    public ResponseEntity<UserResponseDto> create(@Valid @RequestBody UserCreateDto createDto) {
        User userResponse = userService.save(createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserMapper.toDto(userResponse));
    }

    @Operation(summary = "recuperar um usuário por id", responses = {
            @ApiResponse(responseCode = "200", description = "recursos recuperado com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "404", description = "Recursos não encontrado", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorMessage.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getById(@PathVariable Long id) {
        User userResponse = userService.searchById(id);
        return ResponseEntity.status(HttpStatus.OK).body(UserMapper.toDto(userResponse));
    }

    @Operation(summary = "listar todos os usuários com paginação", responses = {
            @ApiResponse(responseCode = "200", description = "recursos recuperados com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class)))
    })
    @GetMapping("")
    public ResponseEntity<Page<UserResponseDto>> getAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<User> users = userService.getAll(pageable);
        return ResponseEntity.status(HttpStatus.OK).body(users.map(UserMapper::toDto));
    }
}
