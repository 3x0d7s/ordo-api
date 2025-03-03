package com.kyut.ordo.task.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.kyut.ordo.board.dto.BoardRead;
import lombok.Data;

@Data
public class TaskListRead {
    private Long id;
    private String title;
    private Integer position;
    private String color;
    private LocalDateTime createdAt;
    private BoardRead board;
    private List<TaskRead> tasks;
}
