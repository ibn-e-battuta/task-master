package io.shinmen.taskmaster.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.shinmen.taskmaster.dto.ApiResponse;
import io.shinmen.taskmaster.dto.ChangePasswordRequest;
import io.shinmen.taskmaster.dto.UpdateProfileRequest;
import io.shinmen.taskmaster.dto.UserProfileResponse;
import io.shinmen.taskmaster.entity.User;
import io.shinmen.taskmaster.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        try {
            String username = authentication.getName();
            User user = userService.getCurrentUser(username);
            UserProfileResponse profile = UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .build();
            return ResponseEntity.ok(profile);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PutMapping("/me")
    public ResponseEntity<?> updateProfile(Authentication authentication, @Valid @RequestBody UpdateProfileRequest updateRequest) {
        try {
            String username = authentication.getName();
            User updatedUser = User.builder()
                .username(updateRequest.getUsername())
                .email(updateRequest.getEmail())
                // Add other profile fields here
                .build();

            userService.updateProfile(username, updatedUser);
            return ResponseEntity.ok(new ApiResponse(true, "Profile updated successfully."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        try {
            String username = authentication.getName();
            userService.changePassword(username, changePasswordRequest.getCurrentPassword(), changePasswordRequest.getNewPassword());
            return ResponseEntity.ok(new ApiResponse(true, "Password changed successfully."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, ex.getMessage()));
        }
    }
}
