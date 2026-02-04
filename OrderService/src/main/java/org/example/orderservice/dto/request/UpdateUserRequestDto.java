package org.example.orderservice.dto.request;

import lombok.Data;

@Data
public class UpdateUserRequestDto {

    private String username;
    private String email;
    private String password;
}
