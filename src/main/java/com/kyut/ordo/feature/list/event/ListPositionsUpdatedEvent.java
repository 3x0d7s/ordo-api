package com.kyut.ordo.feature.list.event;

import com.kyut.ordo.common.DomainEvent;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class ListPositionsUpdatedEvent extends DomainEvent {
    private final Long boardId;
    private final List<Long> listIds;
    
    public ListPositionsUpdatedEvent(Long boardId, List<Long> listIds) {
        super(UUID.randomUUID().toString(), "LIST_POSITIONS_UPDATED");
        this.boardId = boardId;
        this.listIds = listIds;
    }
}