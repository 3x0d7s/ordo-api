package com.kyut.ordo.feature.list.mapper;

import com.kyut.ordo.feature.card.mapper.CardMapper;
import com.kyut.ordo.feature.list.dto.ListCreate;
import com.kyut.ordo.feature.list.dto.ListRead;
import com.kyut.ordo.feature.list.entity.ListEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.kyut.ordo.feature.board.entity.BoardEntity;

@Mapper(componentModel = "spring", uses = {CardMapper.class})
public interface ListMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.position", target = "position")
    @Mapping(source = "dto.color", target = "color")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "board", target = "board")
    @Mapping(target = "cards", ignore = true)
    ListEntity toEntity(ListCreate dto, BoardEntity board);
    
    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.title", target = "title")
    @Mapping(source = "entity.position", target = "position")
    @Mapping(source = "entity.color", target = "color")
    @Mapping(source = "entity.createdAt", target = "createdAt")
    @Mapping(source = "entity.board", target = "board")
    ListRead toDto(ListEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "board", ignore = true)
    @Mapping(target = "cards", ignore = true)
    void updateEntityFromDto(ListCreate dto, @MappingTarget ListEntity taskList);
}
