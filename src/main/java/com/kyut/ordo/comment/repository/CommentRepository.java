package com.kyut.ordo.comment.repository;

import com.kyut.ordo.comment.entity.CommentEntity;
import com.kyut.ordo.task.entity.TaskEntity;
import com.kyut.ordo.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CommentRepository extends CrudRepository<CommentEntity, Long> {
    List<CommentEntity> findAllByCardOrderByCreatedAtDesc(TaskEntity card);

    Page<CommentEntity> findAllByCard(TaskEntity card, Pageable pageable);

    Page<CommentEntity> findAllByCreatedBy(UserEntity user, Pageable pageable);
}
