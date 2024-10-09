package io.shinmen.taskmaster.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.shinmen.taskmaster.dto.AuthResponse;
import io.shinmen.taskmaster.entity.PasswordResetToken;
import io.shinmen.taskmaster.entity.RefreshToken;
import io.shinmen.taskmaster.entity.Role;
import io.shinmen.taskmaster.entity.User;
import io.shinmen.taskmaster.entity.VerificationToken;
import io.shinmen.taskmaster.exception.InvalidCredentialsException;
import io.shinmen.taskmaster.exception.RoleNotFoundException;
import io.shinmen.taskmaster.exception.TokenExpiredException;
import io.shinmen.taskmaster.exception.UserAlreadyExistsException;
import io.shinmen.taskmaster.exception.UserNotFoundException;
import io.shinmen.taskmaster.exception.VerificationTokenNotFoundException;
import io.shinmen.taskmaster.repository.PasswordResetTokenRepository;
import io.shinmen.taskmaster.repository.RoleRepository;
import io.shinmen.taskmaster.repository.UserRepository;
import io.shinmen.taskmaster.repository.VerificationTokenRepository;
import io.shinmen.taskmaster.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final EmailService emailService;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.verification-token.expiration-in-ms}")
    private Long verificationTokenExpiration;

    @Value("${app.password-reset-token.expiration-in-ms}")
    private Long passwordResetTokenExpiration;

    @Transactional
    public void registerUser(User user, String password) {
        if (userRepository.existsByUsername(user.getUsername()))
            throw new UserAlreadyExistsException(user.getUsername());

        if (userRepository.existsByEmail(user.getEmail()))
            throw new UserAlreadyExistsException(user.getEmail());

        user.setPassword(passwordEncoder.encode(password));
        user.setActive(false);
        user.setLocked(false);
        user.setFailedLoginAttempts(0);

        Role userRole = roleRepository.findByName("VIEWER")
                .orElseThrow(() -> new RoleNotFoundException("User Role not set."));
        user.setRoles(new HashSet<>(Arrays.asList(userRole)));

        userRepository.save(user);

        String token = UUID.randomUUID().toString();
        VerificationToken verificationToken = VerificationToken.builder()
                .token(token)
                .user(user)
                .expiryDate(System.currentTimeMillis() + verificationTokenExpiration)
                .build();
        verificationTokenRepository.save(verificationToken);

        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new VerificationTokenNotFoundException(token));

        if (verificationToken.getExpiryDate() < System.currentTimeMillis()) {
            throw new TokenExpiredException(token);
        }

        User user = verificationToken.getUser();
        user.setActive(true);
        userRepository.save(user);

        verificationTokenRepository.delete(verificationToken);
    }

    @Transactional(noRollbackFor = InvalidCredentialsException.class)
    public AuthResponse authenticateUserWithRefreshToken(String username, String password) throws Exception {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            User user = userRepository.findByUsername(username)
                            .orElseThrow(() -> new UserNotFoundException(username));

            user.setFailedLoginAttempts(0);
            userRepository.save(user);

            String accessToken = tokenProvider.generateToken(authentication);
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken.getToken())
                    .build();
        } catch (BadCredentialsException ex) {
            User user = userRepository.findByUsername(username).orElse(null);

            if (user != null) {
                int attempts = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(attempts);
                if (attempts >= 5) { // Example threshold
                    user.setLocked(true);
                }
                userRepository.save(user);
            }
        }

        throw new InvalidCredentialsException("Invalid credentials.");
    }

    @Transactional
    public void initiatePasswordReset(String email) throws Exception {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new Exception("User with given email does not exist."));

        String token = UUID.randomUUID().toString();
        PasswordResetToken resetToken = PasswordResetToken.builder()
                .token(token)
                .user(user)
                .expiryDate(System.currentTimeMillis() + passwordResetTokenExpiration)
                .build();
        passwordResetTokenRepository.save(resetToken);

        // Send password reset email
        emailService.sendPasswordResetEmail(user.getEmail(), token);
    }

    @Transactional
    public void resetPassword(String token, String newPassword) throws Exception {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new Exception("Invalid password reset token."));

        if (resetToken.getExpiryDate() < System.currentTimeMillis()) {
            throw new TokenExpiredException("Password reset token has expired.");
        }

        User user = resetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // Optionally, delete the token after reset
        passwordResetTokenRepository.delete(resetToken);
    }

    public void logoutUser(String token) {
        tokenBlacklistService.blacklistToken(token);
    }

    public String generateAccessToken(User user) {
        // Create an Authentication object based on the user
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                user.getUsername(),
                null,
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                        .collect(Collectors.toList()));

        // Generate JWT token
        return tokenProvider.generateToken(authenticationToken);
    }
}
