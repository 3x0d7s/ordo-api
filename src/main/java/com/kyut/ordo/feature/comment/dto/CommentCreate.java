package com.kyut.ordo.feature.comment.dto;

import lombok.Data;

@Data
public class CommentCreate {
    private String message;
    private Long cardId;
}
