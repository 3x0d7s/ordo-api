package com.kyut.ordo.board.mapper;

import com.kyut.ordo.board.dto.BoardCreate;
import com.kyut.ordo.board.dto.BoardRead;
import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BoardMapper {
    @Mapping(source = "workspaceId", target = "workspace.id")
    BoardEntity toEntity(BoardCreate dto);

    @Mapping(source = "workspace", target = "workspace")
    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.description", target = "description")
    BoardEntity toEntity(BoardCreate dto, WorkspaceEntity workspace);

    BoardRead toDto(BoardEntity entity);

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.description", target = "description")
    BoardRead toDto(BoardEntity entity, WorkspaceEntity workspace);

    void updateEntityFromDto(BoardCreate dto, @MappingTarget BoardEntity board);
}
