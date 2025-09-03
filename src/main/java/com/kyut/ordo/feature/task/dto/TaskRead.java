package com.kyut.ordo.feature.task.dto;

import lombok.Data;

@Data
public class TaskRead {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private Integer position;
}
