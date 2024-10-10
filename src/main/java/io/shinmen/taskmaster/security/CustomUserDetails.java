package io.shinmen.taskmaster.security;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import io.shinmen.taskmaster.entity.User;
import lombok.Getter;

@Getter
public class CustomUserDetails implements UserDetails {

    private UUID id;
    private String username;
    private String email;
    private String password;
    private boolean active;
    private boolean locked;
    private Collection<? extends GrantedAuthority> authorities;

    // Private constructor to enforce the use of the build method
    private CustomUserDetails(UUID id, String username, String email, String password, boolean active, boolean locked,
            Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.active = active;
        this.locked = locked;
        this.authorities = authorities;
    }

    /**
     * Static factory method to create CustomUserDetails from User entity.
     *
     * @param user the User entity
     * @return an instance of CustomUserDetails
     */
    public static CustomUserDetails build(User user) {
        Collection<? extends GrantedAuthority> authorities = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream()) // Assuming Role has getPermissions()
                .map(permission -> (GrantedAuthority) permission::getName)
                .collect(Collectors.toList());

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                user.isActive(),
                user.isLocked(),
                authorities);
    }

    // Implementations of UserDetails interface methods

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    // Username is already defined via getUsername()

    @Override
    public boolean isAccountNonExpired() {
        return true; // Modify if you have account expiration logic
    }

    @Override
    public boolean isAccountNonLocked() {
        return !locked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Modify if you have credential expiration logic
    }

    @Override
    public boolean isEnabled() {
        return active;
    }
}
