package com.kyut.ordo.common;

import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Data
@SuperBuilder
public abstract class DomainEvent {
    private final String eventId;
    private final LocalDateTime occurredAt;
    private final String eventType;
    
    protected DomainEvent(String eventId, String eventType) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.occurredAt = LocalDateTime.now();
    }
}