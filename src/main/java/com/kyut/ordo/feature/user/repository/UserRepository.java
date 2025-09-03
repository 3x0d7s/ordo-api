package com.kyut.ordo.feature.user.repository;

import com.kyut.ordo.feature.user.entity.UserEntity;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserRepository extends CrudRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);
}
