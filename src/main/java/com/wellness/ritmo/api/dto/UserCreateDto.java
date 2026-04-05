package com.wellness.ritmo.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserCreateDto {

    @NotBlank
    private String userName;

    @NotBlank
    @Email(message = "formato do e-mail está invalido")
    private String email;

    @NotBlank
    @PositiveOrZero
    @Size(min = 6, max = 10)
    private String password;

}
