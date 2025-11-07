package com.kyut.ordo.security.auth.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.kyut.ordo.feature.user.dto.UserReadDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    private String accessToken;
    private String refreshToken;
    private String csrfToken;
    private UserReadDTO user;
}
