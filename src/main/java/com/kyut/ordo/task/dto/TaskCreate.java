package com.kyut.ordo.task.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class TaskCreate {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer position;
    private Long listId;
    private Long assignedToId;
}
