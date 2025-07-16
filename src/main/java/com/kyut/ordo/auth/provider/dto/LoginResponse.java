package com.kyut.ordo.auth.provider.dto;

import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private UserReadDTO user;
}
