package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.model.UserProjectRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserProjectRoleRepository extends JpaRepository<UserProjectRole, Long> {
    List<UserProjectRole> findByUserId(Long userId);
    List<UserProjectRole> findByProjectId(Long projectId);
}
