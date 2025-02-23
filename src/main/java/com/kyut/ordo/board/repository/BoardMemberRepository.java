package com.kyut.ordo.board.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kyut.ordo.board.entity.BoardMemberEntity;

@Repository
public interface BoardMemberRepository extends JpaRepository<BoardMemberEntity, Long> {
    Optional<BoardMemberEntity> findByBoardIdAndUserId(Long boardId, Long userId);
    List<BoardMemberEntity> findAllByBoardId(Long boardId);
}
