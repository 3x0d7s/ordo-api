package com.kyut.ordo.task.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.kyut.ordo.user.dto.UserReadDTO;
import lombok.Data;

@Data
public class CardWithItsListRead {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer position;
    private LocalDateTime createdAt;
    private ListRead taskList;
    private UserReadDTO createdBy;
    private UserReadDTO assignedTo;
}
