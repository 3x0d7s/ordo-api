package com.kyut.ordo.task.mapper;

import com.kyut.ordo.task.dto.ListCreate;
import com.kyut.ordo.task.dto.ListRead;
import com.kyut.ordo.task.entity.ListEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.kyut.ordo.board.entity.BoardEntity;

@Mapper(componentModel = "spring", uses = {TaskMapper.class})
public interface TaskListMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.position", target = "position")
    @Mapping(source = "dto.color", target = "color")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "board", target = "board")
    @Mapping(target = "tasks", ignore = true)
    ListEntity toEntity(ListCreate dto, BoardEntity board);
    
    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.position", target = "position")
    @Mapping(source = "entity.color", target = "color")
    @Mapping(source = "entity.createdAt", target = "createdAt")
    @Mapping(source = "entity.board", target = "board")
    ListRead toDto(ListEntity entity);
    
    void updateEntityFromDto(ListCreate dto, @MappingTarget ListEntity taskList);
}
