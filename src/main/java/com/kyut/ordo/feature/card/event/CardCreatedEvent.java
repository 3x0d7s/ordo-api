package com.kyut.ordo.feature.card.event;

import com.kyut.ordo.feature.card.dto.CardWithItsListRead;
import com.kyut.ordo.common.DomainEvent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class CardCreatedEvent extends DomainEvent {
    private final Long cardId;
    private final Long listId;
    private final Long boardId;
    private final CardWithItsListRead cardData;
    
    public CardCreatedEvent(Long cardId, Long listId, Long boardId, CardWithItsListRead cardData) {
        super(UUID.randomUUID().toString(), "CARD_CREATED");
        this.cardId = cardId;
        this.listId = listId;
        this.boardId = boardId;
        this.cardData = cardData;
    }
}
