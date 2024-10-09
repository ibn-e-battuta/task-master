package io.shinmen.taskmaster.dto;

import java.util.Set;
import java.util.UUID;

import io.shinmen.taskmaster.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileResponse {
    private UUID id;
    private String username;
    private String email;
    private Set<Role> roles;
}
