package io.shinmen.taskmaster.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.shinmen.taskmaster.entity.User;
import io.shinmen.taskmaster.exception.UserLockedException;
import io.shinmen.taskmaster.exception.UserNotVerifiedException;
import io.shinmen.taskmaster.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));

        if (!user.isActive())
            throw new UserNotVerifiedException(user.getEmail());

        if (user.isLocked())
            throw new UserLockedException(username);

        return CustomUserDetails.build(user);
    }
}
