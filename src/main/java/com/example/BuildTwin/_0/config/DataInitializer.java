package com.example.BuildTwin._0.config;

import com.example.BuildTwin._0.model.Role;
import com.example.BuildTwin._0.model.User;
import com.example.BuildTwin._0.repository.RoleRepository;
import com.example.BuildTwin._0.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        fixUserRolesTableConstraints();
        seedRoles();
        seedAdminUser();
    }

    private void fixUserRolesTableConstraints() {
        try {
            // Set default timestamp and drop not-null on created_at in user_roles join table
            jdbcTemplate.execute("ALTER TABLE IF EXISTS user_roles ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS user_roles ALTER COLUMN created_at DROP NOT NULL;");
            log.info("Successfully updated 'user_roles' table constraints for created_at.");
        } catch (Exception e) {
            log.debug("Database constraint adjustment on user_roles skipped/already applied: {}", e.getMessage());
        }
    }

    private void seedRoles() {
        List<String> defaultRoles = List.of(
                "ROLE_ADMIN",
                "ROLE_PROJECT_MANAGER",
                "ROLE_SITE_ENGINEER",
                "ROLE_PLANNING_ENGINEER",
                "ROLE_STORE_KEEPER",
                "ROLE_QUALITY_ENGINEER",
                "ROLE_SAFETY_OFFICER",
                "ROLE_EXECUTIVE"
        );

        for (String roleName : defaultRoles) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Initialized system role: {}", roleName);
            }
        }
    }

    private void seedAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseGet(() -> roleRepository.save(Role.builder().name("ROLE_ADMIN").build()));

            User adminUser = User.builder()
                    .username("admin")
                    .email("admin@buildtwin360.com")
                    .password(passwordEncoder.encode("Admin@123"))
                    .status("ACTIVE")
                    .roles(Set.of(adminRole))
                    .build();

            userRepository.save(adminUser);
            log.info("Initialized default administrator: username='admin', email='admin@buildtwin360.com'");
        }
    }
}
