package com.kyut.ordo.feature.comment.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.kyut.ordo.feature.comment.dto.CommentCreate;
import com.kyut.ordo.feature.comment.dto.CommentRead;
import com.kyut.ordo.feature.comment.entity.CommentEntity;
import com.kyut.ordo.feature.card.entity.CardEntity;
import com.kyut.ordo.feature.user.entity.UserEntity;

@Mapper(componentModel = "spring")
public interface CommentMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(source = "dto.message", target = "message")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(source = "createdBy", target = "createdBy")
    @Mapping(source = "card", target = "card")
    CommentEntity toEntity(CommentCreate dto, UserEntity createdBy, CardEntity card);
    
    @Mapping(source = "entity.id", target = "id")
    @Mapping(source = "entity.message", target = "message")
    @Mapping(source = "entity.createdAt", target = "createdAt")
    @Mapping(source = "entity.createdBy", target = "createdBy")
    @Mapping(source = "entity.card", target = "card")
    CommentRead toDto(CommentEntity entity);
    
//    @Mapping(source = "id", target = "id")
//    @Mapping(source = "username", target = "username")
//    @Mapping(source = "email", target = "email")
//    CommentRead.UserRead userToUserRead(UserEntity user);
//
//    @Mapping(source = "id", target = "id")
//    @Mapping(source = "title", target = "title")
//    CommentRead.TaskSummary taskToTaskSummary(TaskEntity task);
}
