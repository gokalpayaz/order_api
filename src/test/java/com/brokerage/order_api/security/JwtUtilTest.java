package com.brokerage.order_api.security;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.jsonwebtoken.JwtException;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String SECRET_KEY = "testsecretkeytestsecretkeytestsecretkeytestsecretkeytestsecretkey";
    private final long EXPIRATION_MS = 3600000; // 1 hour
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET_KEY, EXPIRATION_MS);
    }
    
    @Test
    void generateToken_ShouldCreateValidToken() {
        // Arrange
        String username = "testuser";
        String role = "admin";
        
        // Act
        String token = jwtUtil.generateToken(username, role);
        
        // Assert
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(jwtUtil.isTokenValid(token));
        assertEquals(username, jwtUtil.extractUsername(token));
        assertEquals(role, jwtUtil.extractRole(token));
    }
    
    @Test
    void isTokenValid_ShouldReturnFalseForInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.format";
        
        // Act & Assert
        assertFalse(jwtUtil.isTokenValid(invalidToken));
    }
    
    @Test
    void isTokenValid_ShouldReturnFalseForExpiredToken() throws Exception {
        // Arrange
        JwtUtil shortExpirationJwtUtil = new JwtUtil(SECRET_KEY, 1L); 
        String token = shortExpirationJwtUtil.generateToken("testuser", "user");
        
        // Act
        Thread.sleep(100); 
        
        // Assert
        assertFalse(shortExpirationJwtUtil.isTokenValid(token));
    }
    
    @Test
    void extractUsername_ShouldReturnCorrectUsername() {
        // Arrange
        String username = "testuser";
        String token = jwtUtil.generateToken(username, "user");
        
        // Act
        String extractedUsername = jwtUtil.extractUsername(token);
        
        // Assert
        assertEquals(username, extractedUsername);
    }
    
    @Test
    void extractRole_ShouldReturnCorrectRole() {
        // Arrange
        String role = "admin";
        String token = jwtUtil.generateToken("testuser", role);
        
        // Act
        String extractedRole = jwtUtil.extractRole(token);
        
        // Assert
        assertEquals(role, extractedRole);
    }
    
    @Test
    void extractUsername_ShouldThrowExceptionForInvalidToken() {
        // Arrange
        String invalidToken = "invalid.token.format";
        
        // Act & Assert
        assertThrows(JwtException.class, () -> jwtUtil.extractUsername(invalidToken));
    }
} 