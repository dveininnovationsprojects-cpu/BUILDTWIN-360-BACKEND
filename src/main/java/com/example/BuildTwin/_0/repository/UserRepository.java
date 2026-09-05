package com.example.BuildTwin._0.repository;

import com.example.BuildTwin._0.domain.identity.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Page<User> findByStatus(String status, Pageable pageable);

    Page<User> findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String username, String email, Pageable pageable);

    Page<User> findByStatusAndUsernameContainingIgnoreCaseOrStatusAndEmailContainingIgnoreCase(
            String status1, String username, String status2, String email, Pageable pageable);
}
