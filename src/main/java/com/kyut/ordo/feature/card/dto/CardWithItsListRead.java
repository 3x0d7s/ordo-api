package com.kyut.ordo.feature.card.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.user.dto.UserReadDTO;
import lombok.Data;

@Data
public class CardWithItsListRead {
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
