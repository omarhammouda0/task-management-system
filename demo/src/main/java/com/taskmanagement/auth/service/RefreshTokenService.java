package com.taskmanagement.auth.service;

import com.taskmanagement.auth.entity.RefreshToken;
import com.taskmanagement.user.entity.User;

import java.util.List;
import java.util.Optional;

public interface RefreshTokenService {


    RefreshToken createRefreshToken(User user);


    RefreshToken verifyExpiration(RefreshToken token);


    Optional<RefreshToken> findByToken(String token);


    void deleteByUser(User user);


    void deleteExpiredTokens();


    void revokeToken(RefreshToken token);


    boolean revokeTokenByString(String tokenString);


    int revokeAllUserTokens(User user);


    List<RefreshToken> getActiveTokensByUser(User user);

    List<RefreshToken> getAllTokensByUser(User user);


    boolean isTokenValid(String tokenString);


    RefreshToken rotateToken(RefreshToken oldToken);


    void deleteTokenById(Long tokenId);


    long countActiveTokensByUser(User user);

    void deleteRevokedTokens();


    java.time.Instant getTokenExpiryDate();
}