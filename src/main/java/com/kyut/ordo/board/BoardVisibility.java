package com.kyut.ordo.board;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BoardVisibility {
    PUBLIC("PUBLIC"),
    PRIVATE("PRIVATE"),
    WORKSPACE("WORKSPACE");

    private final String value;
}
