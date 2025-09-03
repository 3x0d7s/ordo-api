package com.kyut.ordo.feature.card.event;

import com.kyut.ordo.common.DomainEvent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class CardPositionsUpdatedEvent extends DomainEvent {
    private final Long listId;
    private final Long boardId;
    private final List<Long> cardIds;
    
    public CardPositionsUpdatedEvent(Long listId, Long boardId, List<Long> cardIds) {
        super(UUID.randomUUID().toString(), "CARD_POSITIONS_UPDATED");
        this.listId = listId;
        this.boardId = boardId;
        this.cardIds = cardIds;
    }
}
