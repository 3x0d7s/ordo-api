package com.kyut.ordo.security.jwt;

import com.kyut.ordo.security.cookie.CookieProperties;
import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class JwtCookieBuilder {
    private String value;
    private int maxAge;
    private boolean secure;
    private String sameSite;

    public JwtCookieBuilder(String value) {
        this.value = value;
        this.maxAge = 3600; // an hour
        this.secure = true;
        this.sameSite = value;
    }

    private static Cookie prepare(String value,
                                  int maxAge,
                                  boolean secure,
                                  String sameSite) {
        Cookie cookie = new Cookie("jwt", value);
        cookie.setHttpOnly(true);
        cookie.setSecure(secure);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);
        cookie.setAttribute("SameSite", sameSite);
        return cookie;
    }

    public static Cookie buildFromEnvironmentProperties(String value,
                                                        int maxAge,
                                                        CookieProperties cookieProperties) {
        return prepare(
                value,
                maxAge,
                cookieProperties.isSecure(),
                cookieProperties.getSameSite()
        );
    }

    public static Cookie buildEmpty() {
        return prepare(
                null,
                0,
                false,
                ""
        );
    }

    public Cookie build() {
        return prepare(
                this.value,
                this.maxAge,
                this.secure,
                this.sameSite
        );
    }
}
