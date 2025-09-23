package com.springapp.kuyaguard.repository;

import com.springapp.kuyaguard.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByEmail(String email);

    // This method will check if a specific email exists in the database.
    // If this returns false, creation of the account will be allowed.
    Boolean existsByEmail(String email);

    // finder method
    Optional<UserEntity> findByUserId(String email);
}
