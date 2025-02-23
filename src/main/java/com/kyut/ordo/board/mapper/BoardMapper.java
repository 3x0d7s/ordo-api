package com.kyut.ordo.board.mapper;

import com.kyut.ordo.board.dto.BoardCreate;
import com.kyut.ordo.board.dto.BoardRead;
import com.kyut.ordo.board.entity.BoardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BoardMapper {
    BoardEntity toEntity(BoardCreate dto);

    BoardRead toDto(BoardEntity entity);

    void updateEntityFromDto(BoardCreate dto, @MappingTarget BoardEntity board);
}
