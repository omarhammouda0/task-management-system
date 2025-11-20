package com.taskmanagement.common.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No token provided - continue without authentication
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String userEmail = jwtService.extractUsername(jwt);

            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    log.debug("Successfully authenticated user: {}", userEmail);
                } else {
                    log.warn("Invalid token for user: {}", userEmail);
                    handleAuthenticationError(response, request, "TOKEN_INVALID", "Token is invalid or expired");
                    return;
                }
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException ex) {
            log.error("JWT token is expired: {}", ex.getMessage());
            handleAuthenticationError(response, request, "TOKEN_EXPIRED", "JWT token has expired. Please login again.");
        } catch (MalformedJwtException ex) {
            log.error("Invalid JWT token format: {}", ex.getMessage());
            handleAuthenticationError(response, request, "TOKEN_MALFORMED", "Invalid JWT token format. Token is malformed.");
        } catch (SignatureException ex) {
            log.error("JWT signature validation failed: {}", ex.getMessage());
            handleAuthenticationError(response, request, "TOKEN_SIGNATURE_INVALID", "Invalid JWT signature. Token has been tampered with.");
        } catch (UsernameNotFoundException ex) {
            log.error("User not found: {}", ex.getMessage());
            handleAuthenticationError(response, request, "USER_NOT_FOUND", "User associated with this token no longer exists.");
        } catch (IllegalArgumentException ex) {
            log.error("JWT token is empty or invalid: {}", ex.getMessage());
            handleAuthenticationError(response, request, "TOKEN_INVALID", "JWT token is empty or invalid.");
        } catch (Exception ex) {
            log.error("Unexpected error during JWT authentication: ", ex);
            handleAuthenticationError(response, request, "AUTHENTICATION_ERROR", "An error occurred during authentication: " + ex.getMessage());
        }
    }

    private void handleAuthenticationError(
            HttpServletResponse response,
            HttpServletRequest request,
            String errorCode,
            String errorMessage) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.UNAUTHORIZED,
                errorMessage
        );
        problemDetail.setTitle("Authentication Failed");
        problemDetail.setProperty("code", errorCode);
        problemDetail.setProperty("timestamp", Instant.now());
        problemDetail.setProperty("path", request.getRequestURI());

        response.getWriter().write(objectMapper.writeValueAsString(problemDetail));
    }
}
