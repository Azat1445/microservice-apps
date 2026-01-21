package org.example.orderservice.dto.response;

import lombok.Data;

@Data
public class UserResponseDto {

    private Long id;
    private String username;
    private String email;
    private String role;
}
