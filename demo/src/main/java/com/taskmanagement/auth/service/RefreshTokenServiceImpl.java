package com.taskmanagement.auth.service;

import com.taskmanagement.auth.entity.RefreshToken;
import com.taskmanagement.auth.repository.RefreshTokenRepository;
import com.taskmanagement.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenServiceImpl implements RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration:604800000}") // 7 days default
    private Long refreshTokenDurationMs;

    @Override
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusMillis(refreshTokenDurationMs);

        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .user(user)
                .expiryDate(expiryDate)
                .revoked(false)
                .build();

        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);
        log.debug("Created refresh token for user: {}, expires: {}", user.getEmail(), expiryDate);

        return savedToken;
    }

    @Override
    @Transactional(readOnly = true)
    public RefreshToken verifyExpiration(RefreshToken token) {

        if (token.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException("Refresh token was expired. Please make a new login request");
        }

        if (token.isRevoked()) {
            throw new RuntimeException("Refresh token was revoked. Please login again");
        }

        return token;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    @Override
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
        log.debug("Deleted all refresh tokens for user: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void deleteExpiredTokens() {
        // ✅ FIXED: Use Instant.now() directly
        Instant now = Instant.now();
        refreshTokenRepository.deleteByExpiryDateBefore(now);
        log.debug("Cleaned up expired refresh tokens");
    }

    @Override
    @Transactional
    public void revokeToken(RefreshToken token) {
        token.setRevoked(true);
        refreshTokenRepository.save(token);
        log.debug("Revoked refresh token: {}", token.getToken());
    }

    @Override
    @Transactional
    public boolean revokeTokenByString(String tokenString) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(tokenString);
        if (tokenOpt.isPresent()) {
            RefreshToken token = tokenOpt.get();
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            log.debug("Revoked refresh token by string: {}", tokenString);
            return true;
        }
        log.debug("Token not found for revocation: {}", tokenString);
        return false;
    }

    @Override
    @Transactional
    public int revokeAllUserTokens(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
        int count = 0;
        for (RefreshToken token : tokens) {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
            count++;
        }
        log.info("Revoked {} tokens for user: {}", count, user.getEmail());
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> getActiveTokensByUser(User user) {
        List<RefreshToken> tokens = refreshTokenRepository.findByUserAndRevokedFalse(user);
        Instant now = Instant.now();
        // Filter out expired tokens
        return tokens.stream()
                .filter(token -> token.getExpiryDate().isAfter(now))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RefreshToken> getAllTokensByUser(User user) {
        return refreshTokenRepository.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isTokenValid(String tokenString) {
        Optional<RefreshToken> tokenOpt = refreshTokenRepository.findByToken(tokenString);
        if (tokenOpt.isEmpty()) {
            return false;
        }

        RefreshToken token = tokenOpt.get();
        Instant now = Instant.now();

        // Valid if not revoked and not expired
        return !token.isRevoked() && token.getExpiryDate().isAfter(now);
    }

    @Override
    @Transactional
    public RefreshToken rotateToken(RefreshToken oldToken) {
        // Revoke the old token
        oldToken.setRevoked(true);
        refreshTokenRepository.save(oldToken);
        log.debug("Revoked old refresh token during rotation: {}", oldToken.getToken());

        // Create new token for the same user
        RefreshToken newToken = createRefreshToken(oldToken.getUser());
        log.debug("Created new refresh token during rotation for user: {}", oldToken.getUser().getEmail());

        return newToken;
    }

    @Override
    @Transactional
    public void deleteTokenById(Long tokenId) {
        refreshTokenRepository.deleteById(tokenId);
        log.debug("Deleted refresh token by ID: {}", tokenId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countActiveTokensByUser(User user) {
        List<RefreshToken> activeTokens = getActiveTokensByUser(user);
        return activeTokens.size();
    }

    @Override
    @Transactional
    public void deleteRevokedTokens() {
        List<RefreshToken> allTokens = refreshTokenRepository.findAll();
        List<RefreshToken> revokedTokens = allTokens.stream()
                .filter(RefreshToken::isRevoked)
                .collect(Collectors.toList());

        refreshTokenRepository.deleteAll(revokedTokens);
        log.info("Deleted {} revoked refresh tokens", revokedTokens.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Instant getTokenExpiryDate() {
        return Instant.now().plusMillis(refreshTokenDurationMs);
    }
}