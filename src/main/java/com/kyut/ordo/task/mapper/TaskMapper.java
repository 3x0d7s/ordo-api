package com.kyut.ordo.task.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.kyut.ordo.task.dto.TaskCreate;
import com.kyut.ordo.task.dto.TaskRead;
import com.kyut.ordo.task.entity.TaskEntity;
import com.kyut.ordo.task.entity.TaskListEntity;
import com.kyut.ordo.user.UserEntity;

@Mapper(componentModel = "spring")
public interface TaskMapper {
    
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
    TaskEntity toEntity(TaskCreate dto, TaskListEntity taskList, UserEntity createdBy, UserEntity assignedTo);
    
    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "entity.dueDate", target = "dueDate")
    @Mapping(source = "entity.position", target = "position")
    @Mapping(source = "entity.createdAt", target = "createdAt")
    @Mapping(source = "entity.taskList", target = "taskList")
    @Mapping(source = "entity.createdBy", target = "createdBy")
    @Mapping(source = "entity.assignedTo", target = "assignedTo")
    TaskRead toDto(TaskEntity entity);
    
    void updateEntityFromDto(TaskCreate dto, @MappingTarget TaskEntity task);
}
