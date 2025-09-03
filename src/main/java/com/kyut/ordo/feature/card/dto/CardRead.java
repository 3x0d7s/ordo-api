package com.kyut.ordo.feature.card.dto;

import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.user.dto.UserReadDTO;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class CardRead {
    private Long id;
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer position;
    private LocalDateTime createdAt;
    private ListRead list;
    private UserReadDTO createdBy;
    private UserReadDTO assignedTo;
}
