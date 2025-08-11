package com.kyut.ordo.card.event;

import com.kyut.ordo.card.dto.CardWithItsListRead;
import com.kyut.ordo.core.common.event.DomainEvent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class CardDeletedEvent extends DomainEvent {
    private final Long cardId;
    private final Long listId;
    private final Long boardId;
    private final CardWithItsListRead cardData;
    
    public CardDeletedEvent(Long cardId, Long listId, Long boardId, CardWithItsListRead cardData) {
        super(UUID.randomUUID().toString(), "CARD_DELETED");
        this.cardId = cardId;
        this.listId = listId;
        this.boardId = boardId;
        this.cardData = cardData;
    }
}
