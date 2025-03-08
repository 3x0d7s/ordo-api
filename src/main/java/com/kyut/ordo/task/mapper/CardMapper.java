package com.kyut.ordo.task.mapper;

import com.kyut.ordo.task.dto.CardRead;
import com.kyut.ordo.task.dto.CardWithItsListRead;
import com.kyut.ordo.task.entity.CardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.kyut.ordo.task.dto.CardCreate;
import com.kyut.ordo.task.entity.ListEntity;
import com.kyut.ordo.user.UserEntity;

@Mapper(componentModel = "spring")
public interface CardMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.dueDate", target = "dueDate")
    @Mapping(source = "dto.position", target = "position")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "taskList", target = "taskList")
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "assignedTo", target = "assignedTo")
    @Mapping(target = "comments", ignore = true)
    CardEntity toEntity(CardCreate dto, ListEntity taskList, UserEntity createdBy, UserEntity assignedTo);
    
    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "entity.dueDate", target = "dueDate")
    @Mapping(source = "entity.position", target = "position")
    @Mapping(source = "entity.createdAt", target = "createdAt")
    @Mapping(source = "entity.taskList", target = "taskList")
    @Mapping(source = "entity.createdBy", target = "createdBy")
    @Mapping(source = "entity.assignedTo", target = "assignedTo")
    CardWithItsListRead toDtoWithItsList(CardEntity entity);

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "entity.dueDate", target = "dueDate")
    @Mapping(source = "entity.position", target = "position")
    @Mapping(source = "entity.createdAt", target = "createdAt")
    @Mapping(source = "entity.createdBy", target = "createdBy")
    @Mapping(source = "entity.assignedTo", target = "assignedTo")
    CardRead toDto(CardEntity entity);
    
    void updateEntityFromDto(CardCreate dto, @MappingTarget CardEntity task);
}
