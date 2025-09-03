package com.kyut.ordo.feature.board.mapper;

import com.kyut.ordo.feature.board.dto.BoardRoleCreate;
import com.kyut.ordo.feature.board.dto.BoardRoleRead;
import com.kyut.ordo.feature.board.dto.BoardRoleUpdate;
import com.kyut.ordo.feature.board.entity.BoardRoleEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BoardRoleMapper {
    BoardRoleRead toDto(BoardRoleEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "members", ignore = true)
    BoardRoleEntity toEntity(BoardRoleCreate dto);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "members", ignore = true)
    void updateEntityFromDto(BoardRoleUpdate dto, @MappingTarget BoardRoleEntity entity);
}
