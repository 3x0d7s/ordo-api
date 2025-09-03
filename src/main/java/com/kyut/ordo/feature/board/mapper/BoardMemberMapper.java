package com.kyut.ordo.feature.board.mapper;

import com.kyut.ordo.feature.board.dto.BoardMemberRead;
import com.kyut.ordo.feature.board.entity.BoardMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BoardMemberMapper {

    @Mapping(target = "joinedAt", source = "joinedAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    BoardMemberRead toDto(BoardMemberEntity entity);
}
