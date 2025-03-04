package com.kyut.ordo.comment.dto;

import java.time.LocalDateTime;

import com.kyut.ordo.task.dto.TaskRead;
import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.Data;

@Data
public class CommentRead {
    private Long id;
    private String message;
    private LocalDateTime createdAt;
    private UserReadDTO createdBy;
    private TaskRead card;
}
