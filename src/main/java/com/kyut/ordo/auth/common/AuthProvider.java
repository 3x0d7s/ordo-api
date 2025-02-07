package com.kyut.ordo.auth.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum AuthProvider {
    LOCAL("LOCAL"),
    GOOGLE("GOOGLE");

    @Getter
    private final String provider;
}
