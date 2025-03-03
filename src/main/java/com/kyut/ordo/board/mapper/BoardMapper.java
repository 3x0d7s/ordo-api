package com.kyut.ordo.board.mapper;

import com.kyut.ordo.board.dto.BoardCreate;
import com.kyut.ordo.board.dto.BoardRead;
import com.kyut.ordo.board.entity.BoardEntity;
import com.kyut.ordo.board.entity.BoardRoleEntity;
import com.kyut.ordo.workspace.entity.WorkspaceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.Collection;

@Mapper(componentModel = "spring")
public interface BoardMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.visibility", target = "visibility")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "workspaceId", target = "workspace.id")
    BoardEntity toEntity(BoardCreate dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.visibility", target = "visibility")
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "members", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "workspaceEntity", target = "workspace")
    BoardEntity toEntity(BoardCreate dto, WorkspaceEntity workspaceEntity);

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "entity.roles", target = "roles")
    BoardRead toDto(BoardEntity entity);

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "entity.roles", target = "roles")
    BoardRead toDto(BoardEntity entity, WorkspaceEntity workspace);

    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.description", target = "description")
    @Mapping(source = "rolesCollection", target = "roles")
    BoardRead toDto(BoardEntity entity, Collection<BoardRoleEntity> rolesCollection);

    void updateEntityFromDto(BoardCreate dto, @MappingTarget BoardEntity board);
}
