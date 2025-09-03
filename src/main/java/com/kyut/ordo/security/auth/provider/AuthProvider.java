package com.kyut.ordo.security.auth.provider;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthProvider {
    LOCAL("LOCAL"),
    GOOGLE("GOOGLE");

    private final String provider;
}
