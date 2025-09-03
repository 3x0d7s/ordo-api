package com.kyut.ordo.feature.task.event;

import com.kyut.ordo.common.DomainEvent;
import com.kyut.ordo.feature.task.dto.TaskRead;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

/**
 * Подія видалення завдання
 */
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
public class TaskDeletedEvent extends DomainEvent {
    private final Long taskId;
    private final Long cardId;
    private final TaskRead taskData;
    
    public TaskDeletedEvent(Long taskId, Long cardId, TaskRead taskData) {
        super(UUID.randomUUID().toString(), "TASK_DELETED");
        this.taskId = taskId;
        this.cardId = cardId;
        this.taskData = taskData;
    }
}
