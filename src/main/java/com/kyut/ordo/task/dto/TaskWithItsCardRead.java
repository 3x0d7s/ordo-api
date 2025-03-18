package com.kyut.ordo.task.dto;

import com.kyut.ordo.card.dto.CardRead;
import lombok.Data;

@Data
public class TaskWithItsCardRead {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private Integer position;
    private CardRead card;
}
