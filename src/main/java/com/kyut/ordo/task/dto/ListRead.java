package com.kyut.ordo.task.dto;

import java.time.LocalDateTime;

import com.kyut.ordo.board.dto.BoardRead;
import lombok.Data;

@Data
public class ListRead {
    private Long id;
    private String title;
    private Integer position;
    private String color;
    private LocalDateTime createdAt;
    private BoardRead board;
}
