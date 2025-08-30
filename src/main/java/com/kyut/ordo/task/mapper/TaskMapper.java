package com.kyut.ordo.task.mapper;

import com.kyut.ordo.task.dto.TaskWithItsCardRead;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.kyut.ordo.card.entity.CardEntity;
import com.kyut.ordo.task.dto.TaskCreate;
import com.kyut.ordo.task.dto.TaskRead;
import com.kyut.ordo.task.entity.TaskEntity;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.position", target = "position")
    @Mapping(source = "dto.completed", target = "completed")
    @Mapping(source = "card", target = "card")
    TaskEntity toEntity(TaskCreate dto, CardEntity card);
    
    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "entity.position", target = "position")
    @Mapping(source = "entity.completed", target = "completed")
    TaskRead toDto(TaskEntity entity);

    @Mapping(source = "entity.card", target = "card")
    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "entity.position", target = "position")
    @Mapping(source = "entity.completed", target = "completed")
    TaskWithItsCardRead toDtoWithItsCard(TaskEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "card", ignore = true)
    void updateEntityFromDto(TaskCreate dto, @MappingTarget TaskEntity task);
}
