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

    // Exactly the 10 Stakeholder & User Roles from BuildTwin 360 SRS Section 7
    private static final List<String> EXACT_SPECIFICATION_ROLES = List.of(
            "ROLE_DIRECTOR",                   // 1. Director / Management
            "ROLE_PROJECT_MANAGER",            // 2. Project Manager
            "ROLE_SITE_ENGINEER",              // 3. Site Engineer
            "ROLE_SITE_SUPERVISOR",            // 4. Site Supervisor
            "ROLE_PROCUREMENT_STORE",          // 5. Procurement / Store
            "ROLE_QUANTITY_COST_COORDINATOR",  // 6. Quantity / Cost Coordinator
            "ROLE_QUALITY_ENGINEER",           // 7. Quality Engineer
            "ROLE_DATA_ANALYST",               // 8. Data / Management Analyst
            "ROLE_ADMIN",                      // 9. System Administrator
            "ROLE_AUDITOR"                     // 10. Auditor / Reviewer
    );

    @Override
    public void run(String... args) {
        fixUserRolesTableConstraints();
        cleanAndSyncExactRoles();
        seedAdminUser();
    }

    private void fixUserRolesTableConstraints() {
        try {
            // Set default timestamp and drop not-null on created_at in user_roles join table
            jdbcTemplate.execute("ALTER TABLE IF EXISTS user_roles ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;");
            jdbcTemplate.execute("ALTER TABLE IF EXISTS user_roles ALTER COLUMN created_at DROP NOT NULL;");
            log.info("Updated 'user_roles' table constraints for created_at.");
        } catch (Exception e) {
            log.debug("Database constraint adjustment on user_roles skipped: {}", e.getMessage());
        }
    }

    /**
     * Seeds strictly the 10 roles specified in BuildTwin 360 SRS Section 7
     * and removes any non-specification roles.
     */
    private void cleanAndSyncExactRoles() {
        // 1. Delete user role associations for any extra roles first
        try {
            jdbcTemplate.execute(
                    "DELETE FROM user_roles WHERE role_id IN (" +
                    "   SELECT id FROM roles WHERE name NOT IN (" +
                    "       'ROLE_DIRECTOR', 'ROLE_PROJECT_MANAGER', 'ROLE_SITE_ENGINEER', " +
                    "       'ROLE_SITE_SUPERVISOR', 'ROLE_PROCUREMENT_STORE', 'ROLE_QUANTITY_COST_COORDINATOR', " +
                    "       'ROLE_QUALITY_ENGINEER', 'ROLE_DATA_ANALYST', 'ROLE_ADMIN', 'ROLE_AUDITOR'" +
                    "   )" +
                    ");"
            );
            // 2. Delete the extra roles from roles table
            jdbcTemplate.execute(
                    "DELETE FROM roles WHERE name NOT IN (" +
                    "   'ROLE_DIRECTOR', 'ROLE_PROJECT_MANAGER', 'ROLE_SITE_ENGINEER', " +
                    "   'ROLE_SITE_SUPERVISOR', 'ROLE_PROCUREMENT_STORE', 'ROLE_QUANTITY_COST_COORDINATOR', " +
                    "   'ROLE_QUALITY_ENGINEER', 'ROLE_DATA_ANALYST', 'ROLE_ADMIN', 'ROLE_AUDITOR'" +
                    ");"
            );
        } catch (Exception e) {
            log.debug("Roles table cleanup skipped: {}", e.getMessage());
        }

        // 3. Ensure all 10 standard roles exist
        for (String roleName : EXACT_SPECIFICATION_ROLES) {
            if (roleRepository.findByName(roleName).isEmpty()) {
                roleRepository.save(Role.builder().name(roleName).build());
                log.info("Initialized SRS Section 7 Role: {}", roleName);
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
