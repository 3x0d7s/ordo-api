package com.kyut.ordo.board.mapper;

import com.kyut.ordo.board.dto.BoardRoleRead;
import com.kyut.ordo.board.entity.BoardRoleEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface BoardRoleMapper {
    BoardRoleRead toDto(BoardRoleEntity entity);

    BoardRoleEntity toEntity(BoardRoleRead dto);
}
