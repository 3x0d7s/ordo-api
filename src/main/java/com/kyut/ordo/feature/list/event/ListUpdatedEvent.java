package com.kyut.ordo.feature.list.event;

import com.kyut.ordo.common.DomainEvent;
import com.kyut.ordo.feature.list.dto.ListRead;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class ListUpdatedEvent extends DomainEvent {
    private final Long listId;
    private final Long boardId;
    private final ListRead listData;
    
    public ListUpdatedEvent(Long listId, Long boardId, ListRead listData) {
        super(UUID.randomUUID().toString(), "LIST_UPDATED");
        this.listId = listId;
        this.boardId = boardId;
        this.listData = listData;
    }
}