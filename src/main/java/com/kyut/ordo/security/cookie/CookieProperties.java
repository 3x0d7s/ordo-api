package com.kyut.ordo.security.cookie;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.cookie")
@Getter
@Setter
public class CookieProperties {
    // Whether to mark cookies as Secure (HTTPS only). For local http dev you may set this to false.
    private boolean secure;
    // SameSite policy. Use "None" for cross-site SPA <-> API on different ports.
    private String sameSite;
}



