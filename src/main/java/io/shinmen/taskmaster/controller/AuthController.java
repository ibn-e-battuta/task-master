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
import io.shinmen.taskmaster.exception.TokenExpiredException;
import io.shinmen.taskmaster.exception.TokenNotFoundException;
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
        User user = User.builder()
                .username(registerRequest.getUsername())
                .email(registerRequest.getEmail())
                .build();

        authService.registerUser(user, registerRequest.getPassword());

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("User registered successfully. Please check your email for verification.")
                .build();

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/verify")
    public ResponseEntity<ApiResponse> verifyUser(@RequestParam String token) {
        authService.verifyEmail(token);

        ApiResponse response = ApiResponse.builder()
                .success(true)
                .message("Email verified successfully.")
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> authenticateUser(@Valid @RequestBody LoginRequest loginRequest)
            throws Exception {
        AuthResponse authResponse = authService.authenticateUserWithRefreshToken(
                loginRequest.getUsername(),
                loginRequest.getPassword());
        return ResponseEntity.ok(authResponse);
    }

    @PostMapping("/password-reset-request")
    public ResponseEntity<ApiResponse> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request)
            throws Exception {
        authService.initiatePasswordReset(request.getEmail());
        return ResponseEntity.ok(new ApiResponse(true, "Password reset email sent."));
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
    public ResponseEntity<ApiResponse> logoutUser(@RequestHeader(name = "Authorization") String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);
            authService.logoutUser(token);

            ApiResponse response = ApiResponse.builder()
                    .success(true)
                    .message("Logged out successfully.")
                    .build();

            return ResponseEntity.ok(response);
        }

        ApiResponse response = ApiResponse.builder()
                .success(false)
                .message("Invalid Authorization header.")
                .build();

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshToken refreshToken = refreshTokenService.findByToken(request.getRefreshToken())
                    .orElseThrow(() -> new TokenNotFoundException("Refresh token not found."));

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
        } catch (TokenNotFoundException | TokenExpiredException ex) {
            ApiResponse apiResponse = ApiResponse.builder()
                    .success(false)
                    .message(ex.getMessage())
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.FORBIDDEN);
        } catch (Exception ex) {
            ApiResponse apiResponse = ApiResponse.builder()
                    .success(false)
                    .message("Could not refresh token.")
                    .build();
            return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
