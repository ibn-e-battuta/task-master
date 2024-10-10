package io.shinmen.taskmaster.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.shinmen.taskmaster.entity.User;
import io.shinmen.taskmaster.exception.SamePasswordException;
import io.shinmen.taskmaster.exception.UserNotFoundException;
import io.shinmen.taskmaster.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User getCurrentUser(String username) {
        return userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));
    }

    @Transactional
    public void updateProfile(String username, User updatedUser) {
        User existingUser = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        // Update other profile fields as necessary

        userRepository.save(existingUser);
    }

    @Transactional
    public void changePassword(String username, String currentPassword, String newPassword) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UserNotFoundException(username));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new SamePasswordException();
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
}
