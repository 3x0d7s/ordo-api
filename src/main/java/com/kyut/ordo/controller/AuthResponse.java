package com.kyut.ordo.controller;

import com.kyut.ordo.user.UserDTO;
import lombok.*;

@Builder
@AllArgsConstructor
@Data
public class AuthResponse {
    private String access_token;
    private String refresh_token;
    private UserDTO user;
}
