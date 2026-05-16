package com.ga.capstone.seeders;


import com.ga.capstone.enums.UserStatus;
import com.ga.capstone.models.Permission;
import com.ga.capstone.models.Role;
import com.ga.capstone.models.User;
import com.ga.capstone.repositories.PermissionRepository;
import com.ga.capstone.repositories.RoleRepository;
import com.ga.capstone.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * Seeds the database with initial data on first application startup.
 * Creates permissions, roles (ADMIN + USER), and a default admin user.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {
    private final PermissionRepository permissionRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting database seeding...");

        seedPermissions();
        seedRoles();

        log.info("Database seeding completed!");
    }

    /**
     * Seed all CRUD permissions for each model in the application.
     * Permission format: "modelname:action" (e.g., "course:create")
     */
    private void seedPermissions() {
        log.info("Seeding permissions...");

        // Check if permissions already exist
        if (permissionRepository.count() > 0) {
            log.info("Permissions already exist. Skipping permission seeding.");
            return;
        }

        // All models in the LearnHub application
        String[] models = {"permission", "role", "user", "userprofile", "category", "course", "lesson", "enrollment"};

        String[] actions = {"create", "update", "delete", "read"};

        List<Permission> permissions = new ArrayList<>();

        for (String model : models) {
            for (String action : actions) {
                // Store as lowercase "model:action"
                Permission permission = Permission.builder().action(model + ":" + action).build();
                permissions.add(permission);
            }
        }

        permissionRepository.saveAll(permissions);
        log.info("Created {} permissions", permissions.size());
    }

    /**
     * Seed ADMIN and USER roles with appropriate permissions.
     * ADMIN gets all permissions; USER gets limited permissions.
     */
    private void seedRoles() {
        log.info("Seeding roles...");

        if (roleRepository.count() > 0) {
            log.info("Roles already exist. Skipping role seeding.");
            return;
        }

        // Get all permissions and build lookup map
        List<Permission> allPermissions = permissionRepository.findAll();
        Map<String, Permission> permissionMap = new HashMap<>();
        for (Permission p : allPermissions) {
            permissionMap.put(p.getAction(), p);
        }

        // Create Admin Role - has all permissions
        Role adminRole = new Role();
        adminRole.setName("ADMIN");
        adminRole.setDescription("Administrator with full access to all resources");
        adminRole.setPermissions(new HashSet<>(allPermissions));
        roleRepository.save(adminRole);
        log.info("Created ADMIN role with {} permissions", allPermissions.size());

        // Seed the default admin user
        seedAdminUser(adminRole);

        // Create User Role (students/instructors)
        Role userRole = new Role();
        userRole.setName("USER");
        userRole.setDescription("Regular user - can enroll in courses, manage own profile");
        Set<Permission> userPermissions = new HashSet<>();

        // User permissions (using lowercase keys matching stored permission format)
        addPermissionsForModel(userPermissions, permissionMap, "userprofile", Arrays.asList("create", "update", "read"));
        addPermissionsForModel(userPermissions, permissionMap, "course", Arrays.asList("create", "read", "update"));
        addPermissionsForModel(userPermissions, permissionMap, "lesson", Arrays.asList("create", "read", "update"));
        addPermissionsForModel(userPermissions, permissionMap, "enrollment", Arrays.asList("create", "read", "delete"));
        addPermissionsForModel(userPermissions, permissionMap, "category", List.of("read"));

        userRole.setPermissions(userPermissions);
        roleRepository.save(userRole);
        log.info("Created USER role with {} permissions", userPermissions.size());
    }


    /**
     * Seed a default admin user for development/testing.
     *
     * @param adminRole the admin role to assign to the user
     */
    private void seedAdminUser(Role adminRole) {
        String adminEmail = "admin@learnhub.com";
        String adminPassword = "Admin123!";

        // Skip if admin already exists
        if (userRepository.existsByEmail(adminEmail)) {
            log.info("Admin user already exists. Skipping admin seeding.");
            return;
        }

        User admin = new User();
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setRole(adminRole);
        admin.setEmailVerified(true);
        admin.setStatus(UserStatus.ACTIVE);
        admin.setDeleted(false);

        userRepository.save(admin);

        log.info("Created ADMIN user: {} / {}", adminEmail, adminPassword);
        log.warn("CHANGE DEFAULT ADMIN PASSWORD IN PRODUCTION!");
    }


    /**
     * Helper to add permissions for a specific model to a permission set.
     *
     * @param permissions  the set to add permissions to
     * @param permissionMap lookup map of action -> Permission entity
     * @param model        the model name (lowercase, e.g., "course")
     * @param actions      list of actions (e.g., "create", "read")
     */
    private void addPermissionsForModel(Set<Permission> permissions, Map<String, Permission> permissionMap, String model, List<String> actions) {
        for (String action : actions) {
            // Key format matches stored permissions: "model:action" (all lowercase)
            String key = model + ":" + action;
            Permission permission = permissionMap.get(key);
            if (permission != null) {
                permissions.add(permission);
            } else {
                log.warn("Permission not found: {}", key);
            }
        }
    }
}
