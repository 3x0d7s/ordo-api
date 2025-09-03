package com.kyut.ordo.security.auth.provider.oauth2;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.security.oauth2.client.registration.google")
public class GoogleOAuth2Properties {
    private String clientId;
    private String clientSecret;
    private String redirectUri;
    private String userInfoUrl = "https://www.googleapis.com/oauth2/v2/userinfo";
}
