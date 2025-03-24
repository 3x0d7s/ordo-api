package com.kyut.ordo.board.mapper;

import com.kyut.ordo.board.dto.BoardMemberRead;
import com.kyut.ordo.board.entity.BoardMemberEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface BoardMemberMapper {
    
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "joinedAt", source = "joinedAt", dateFormat = "yyyy-MM-dd HH:mm:ss")
    BoardMemberRead toDto(BoardMemberEntity entity);
}
