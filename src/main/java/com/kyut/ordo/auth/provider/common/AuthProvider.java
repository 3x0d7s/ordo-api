package com.kyut.ordo.auth.provider.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AuthProvider {
    LOCAL("LOCAL"),
    GOOGLE("GOOGLE");

    private final String provider;
}
