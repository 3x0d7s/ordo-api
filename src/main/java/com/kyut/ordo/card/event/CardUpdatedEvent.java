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
public class CardUpdatedEvent extends DomainEvent {
    private final Long cardId;
    private final Long listId;
    private final Long boardId;
    private final Long oldListId;
    private final CardWithItsListRead cardData;
    private final boolean listChanged;
    
    public CardUpdatedEvent(Long cardId, Long listId, Long boardId, CardWithItsListRead cardData) {
        this(cardId, listId, boardId, null, cardData, false);
    }
    
    public CardUpdatedEvent(Long cardId, Long listId, Long boardId, Long oldListId, CardWithItsListRead cardData, boolean listChanged) {
        super(UUID.randomUUID().toString(), "CARD_UPDATED");
        this.cardId = cardId;
        this.listId = listId;
        this.boardId = boardId;
        this.oldListId = oldListId;
        this.cardData = cardData;
        this.listChanged = listChanged;
    }
}
