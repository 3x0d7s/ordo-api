package com.kyut.ordo.board.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kyut.ordo.board.entity.BoardRoleEntity;

@Repository
public interface BoardRoleRepository extends JpaRepository<BoardRoleEntity, Long> {
    Optional<BoardRoleEntity> findByName(String name);
}
