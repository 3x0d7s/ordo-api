package com.kyut.ordo.auth.oauth2.dto;

import com.kyut.ordo.user.UserDTO;
import lombok.*;

@Builder
@AllArgsConstructor
@Data
public class OAuth2UserInfo {
    private String accessToken;
    private String refreshToken;
    private UserDTO user;
}
