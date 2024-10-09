package io.shinmen.taskmaster.config;

import java.util.Arrays;
import java.util.HashSet;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.shinmen.taskmaster.entity.Permission;
import io.shinmen.taskmaster.entity.Role;
import io.shinmen.taskmaster.repository.PermissionRepository;
import io.shinmen.taskmaster.repository.RoleRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) throws Exception {
        // Define Permissions
        Permission createProject = createPermission("CREATE_PROJECT");
        Permission deleteProject = createPermission("DELETE_PROJECT");
        Permission viewProject = createPermission("VIEW_PROJECT");

        Permission createTask = createPermission("CREATE_TASK");
        Permission deleteTask = createPermission("DELETE_TASK");
        Permission viewTask = createPermission("VIEW_TASK");
        Permission updateTask = createPermission("UPDATE_TASK");
        Permission assignTask = createPermission("ASSIGN_TASK");

        // Define Roles and assign Permissions
        Role admin = Role.builder()
                .name("ADMIN")
                .permissions(new HashSet<>(Arrays.asList(
                        createProject, deleteProject, viewProject,
                        createTask, deleteTask, viewTask, updateTask, assignTask
                )))
                .build();
        roleRepository.save(admin);

        Role projectManager = Role.builder()
                .name("PROJECT_MANAGER")
                .permissions(new HashSet<>(Arrays.asList(
                        createProject, deleteProject, viewProject,
                        createTask, deleteTask, viewTask, updateTask, assignTask
                )))
                .build();
        roleRepository.save(projectManager);

        Role teamMember = Role.builder()
                .name("TEAM_MEMBER")
                .permissions(new HashSet<>(Arrays.asList(
                        viewProject, viewTask, updateTask, assignTask
                )))
                .build();
        roleRepository.save(teamMember);

        Role viewer = Role.builder()
                .name("VIEWER")
                .permissions(new HashSet<>(Arrays.asList(
                        viewProject, viewTask
                )))
                .build();
        roleRepository.save(viewer);
    }

    private Permission createPermission(String name) {
        return permissionRepository.findByName(name).orElseGet(() -> {
            Permission permission = Permission.builder()
                    .name(name)
                    .build();
            return permissionRepository.save(permission);
        });
    }
}
