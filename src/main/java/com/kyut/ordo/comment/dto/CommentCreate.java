package com.kyut.ordo.comment.dto;

import lombok.Data;

@Data
public class CommentCreate {
    private String message;
    private Long cardId;
}
