package com.kyut.ordo.task.dto;

import lombok.Data;

@Data
public class ListCreate {
    private String title;
    private Integer position;
    private String color;
    private Long boardId;
}
