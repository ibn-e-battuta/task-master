package io.shinmen.taskmaster.service;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.shinmen.taskmaster.entity.Permission;
import io.shinmen.taskmaster.entity.Role;
import io.shinmen.taskmaster.entity.User;
import io.shinmen.taskmaster.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (!userOpt.isPresent()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
            if (!userOpt.isPresent()) {
                throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
            }
        }

        User user = userOpt.get();

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isActive(),
                true,
                !user.isLocked(),
                true,
                getAuthorities(user.getRoles())
        );
    }

    private Collection<? extends GrantedAuthority> getAuthorities(Set<Role> roles) {
        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        for (Role role : roles) {
            // Add role as authority
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            // Add permissions as authorities
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }
        return authorities;
    }
}
