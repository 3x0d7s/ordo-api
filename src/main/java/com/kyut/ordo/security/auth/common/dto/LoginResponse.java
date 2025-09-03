package com.kyut.ordo.security.auth.common.dto;

import com.kyut.ordo.feature.user.dto.UserReadDTO;
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
