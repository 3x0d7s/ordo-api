package com.kyut.ordo.card.mapper;

import com.kyut.ordo.card.dto.CardRead;
import com.kyut.ordo.card.dto.CardWithItsListRead;
import com.kyut.ordo.card.entity.CardEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.kyut.ordo.card.dto.CardCreate;
import com.kyut.ordo.list.entity.ListEntity;
import com.kyut.ordo.user.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface CardMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.title", target = "title")
    @Mapping(source = "dto.description", target = "description")
    @Mapping(source = "dto.dueDate", target = "dueDate")
    @Mapping(source = "dto.position", target = "position")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "list", target = "list")
    @Mapping(target = "comments", ignore = true)
    @Mapping(target = "tasks", ignore = true)
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "assignedTo", target = "assignedTo")
    CardEntity toEntity(CardCreate dto, ListEntity list, UserEntity createdBy, UserEntity assignedTo);

    CardWithItsListRead toDtoWithItsList(CardEntity entity);

    CardRead toDto(CardEntity entity);
    
    void updateEntityFromDto(CardCreate dto, @MappingTarget CardEntity task);
}
