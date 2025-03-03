package com.kyut.ordo.task.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class TaskRead {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer position;
    private LocalDateTime createdAt;
    private TaskListRead list;
    private UserRead createdBy;
    private UserRead assignedTo;
    
    @Data
    public static class UserRead {
        private Long id;
        private String username;
        private String email;
    }
}
