package com.wellness.ritmo.api.controller;

import com.wellness.ritmo.api.dto.UserCreateDto;
import com.wellness.ritmo.api.dto.UserResponseDto;
import com.wellness.ritmo.api.dto.mapper.UserMapper;
import com.wellness.ritmo.domain.model.User;
import com.wellness.ritmo.domain.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gerenciamento de usuários")
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Criar novo usuário",
            description = "Cria um novo usuário no sistema"
    )
    @ApiResponse(responseCode = "201", description = "Usuário criado com sucesso")
    @ApiResponse(responseCode = "409", description = "Usuário ou email já cadastrado no sistema")
    @ApiResponse(responseCode = "422", description = "Dados de entrada inválidos")
    public UserResponseDto create(@Valid @RequestBody UserCreateDto createDto) {
        User userResponse = userService.save(createDto);
        return UserMapper.toDto(userResponse);
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Recuperar usuário por ID",
            description = "Retorna os dados de um usuário específico"
    )
    @ApiResponse(responseCode = "200", description = "Usuário recuperado com sucesso")
    @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    public UserResponseDto getById(@PathVariable Long id) {
        User userResponse = userService.searchById(id);
        return UserMapper.toDto(userResponse);
    }

    @GetMapping
    @Operation(
            summary = "Listar todos os usuários",
            description = "Retorna uma lista paginada de todos os usuários"
    )
    @ApiResponse(responseCode = "200", description = "Lista de usuários recuperada com sucesso")
    public Page<UserResponseDto> getAll(
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<User> users = userService.getAll(pageable);
        return users.map(UserMapper::toDto);
    }
}
