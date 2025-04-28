package com.kyut.ordo.card.dto;

import lombok.Data;
import java.util.List;

@Data
public class CardPositionUpdate {
    private Long listId;
    private List<Long> cardIds; // Упорядкований список ID карток
}
