package com.kyut.ordo.feature.comment.dto;

import java.time.LocalDateTime;

import com.kyut.ordo.feature.card.dto.CardWithItsListRead;
import com.kyut.ordo.feature.user.dto.UserReadDTO;
import lombok.Data;

@Data
public class CommentRead {
    private Long id;
    private String message;
    private LocalDateTime createdAt;
    private UserReadDTO createdBy;
    private CardWithItsListRead card;
}
