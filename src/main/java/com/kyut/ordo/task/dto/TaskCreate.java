package com.kyut.ordo.task.dto;

import lombok.Data;

@Data
public class TaskCreate {
    private String title;
    private String description;
    private Integer position;
    private Boolean completed;
    private Long cardId;
}
