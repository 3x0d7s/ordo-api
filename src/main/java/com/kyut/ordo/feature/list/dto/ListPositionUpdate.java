package com.kyut.ordo.feature.list.dto;

import lombok.Data;
import java.util.List;

@Data
public class ListPositionUpdate {
    private Long boardId;
    private List<Long> listIds; // Упорядкований список ID списків
}
