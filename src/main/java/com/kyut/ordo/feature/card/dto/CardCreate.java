package com.kyut.ordo.feature.card.dto;

import java.time.LocalDate;
import lombok.Data;

@Data
public class CardCreate {
    private String title;
    private String description;
    private LocalDate dueDate;
    private Integer position;
    private Long listId;
    private Long assignedToId;
}
