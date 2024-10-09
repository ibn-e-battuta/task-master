package io.shinmen.taskmaster.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.shinmen.taskmaster.dto.ApiResponse;
import io.shinmen.taskmaster.dto.AuthResponse;
import io.shinmen.taskmaster.dto.LoginRequest;
import io.shinmen.taskmaster.dto.PasswordResetForm;
import io.shinmen.taskmaster.dto.PasswordResetRequest;
import io.shinmen.taskmaster.dto.RefreshTokenRequest;
import io.shinmen.taskmaster.dto.RefreshTokenResponse;
import io.shinmen.taskmaster.dto.RegisterRequest;
import io.shinmen.taskmaster.entity.RefreshToken;
import io.shinmen.taskmaster.entity.User;
import io.shinmen.taskmaster.service.AuthService;
import io.shinmen.taskmaster.service.RefreshTokenService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final RefreshTokenService refreshTokenService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse> registerUser(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            // Create User entity from DTO
            User user = User.builder()
                    .username(registerRequest.getUsername())
                    .email(registerRequest.getEmail())
                    .build();

            authService.registerUser(user, registerRequest.getPassword());

            return ResponseEntity.status(HttpStatus.CREATED).body(
                    new ApiResponse(true, "User registered successfully. Please check your email for verification."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyUser(@RequestParam("token") String token) {
        try {
            authService.verifyEmail(token);
            return ResponseEntity.ok(new ApiResponse(true, "Email verified successfully."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            AuthResponse authResponse = authService.authenticateUserWithRefreshToken(
                    loginRequest.getUsernameOrEmail(),
                    loginRequest.getPassword());
            return ResponseEntity.ok(authResponse);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<ApiResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
        try {
            authService.initiatePasswordReset(request.getEmail());
            return ResponseEntity.ok(new ApiResponse(true, "Password reset email sent."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PostMapping("/password-reset")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody PasswordResetForm resetForm) {
        try {
            authService.resetPassword(resetForm.getToken(), resetForm.getNewPassword());
            return ResponseEntity.ok(new ApiResponse(true, "Password reset successfully."));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ApiResponse(false, ex.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            authService.logoutUser(token);
            return ResponseEntity.ok(new ApiResponse(true, "Logged out successfully."));
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ApiResponse(false, "Invalid Authorization header."));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                    .orElseThrow(() -> new Exception("Refresh token not found."));

            refreshTokenService.verifyExpiration(refreshToken);

            User user = refreshToken.getUser();
            String newAccessToken = authService.generateAccessToken(user);

            // Optionally, generate a new refresh token and invalidate the old one
            RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);
            refreshTokenService.deleteByUser(user); // Deletes old refresh token

            RefreshTokenResponse response = RefreshTokenResponse.builder()
                    .accessToken(newAccessToken)
                    .refreshToken(newRefreshToken.getToken())
                    .build();

            return ResponseEntity.ok(response);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResponse(false, ex.getMessage()));
        }
    }
}
