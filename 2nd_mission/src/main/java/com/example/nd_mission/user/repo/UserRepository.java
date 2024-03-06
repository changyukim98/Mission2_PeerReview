package com.example.nd_mission.user.repo;

import com.example.nd_mission.user.entity.UserEntity;
import com.example.nd_mission.user.entity.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    boolean existsByUsername(String username);

    List<UserEntity> findByRoleAndBusinessNumIsNotNull(UserRole userRole);
}
