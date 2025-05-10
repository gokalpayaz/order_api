package com.brokerage.order_api.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.brokerage.order_api.model.Customer;

class AuthUtilTest {
    
    private Customer customer;
    
    @BeforeEach
    void setUp() {
        customer = Customer.builder()
                .id(1L)
                .username("testuser")
                .build();
        
        SecurityContextHolder.clearContext();
    }
    
    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }
    
    @Test
    void isAuthorizedForCustomer_SameUser_ReturnsTrue() {
        // Arrange
        Authentication auth = createAuthentication("testuser", Collections.emptyList());
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // Act
        boolean result = AuthUtil.isAuthorizedForCustomer(customer);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void isAuthorizedForCustomer_DifferentUserWithAdminRole_ReturnsTrue() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_admin")
        );
        Authentication auth = createAuthentication("adminuser", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // Act
        boolean result = AuthUtil.isAuthorizedForCustomer(customer);
        
        // Assert
        assertTrue(result);
    }
    
    @Test
    void isAuthorizedForCustomer_DifferentUserWithoutAdminRole_ReturnsFalse() {
        // Arrange
        Collection<GrantedAuthority> authorities = Arrays.asList(
                new SimpleGrantedAuthority("ROLE_user")
        );
        Authentication auth = createAuthentication("otheruser", authorities);
        SecurityContextHolder.getContext().setAuthentication(auth);
        
        // Act
        boolean result = AuthUtil.isAuthorizedForCustomer(customer);
        
        // Assert
        assertFalse(result);
    }
    
    private Authentication createAuthentication(String username, Collection<GrantedAuthority> authorities) {
        return new UsernamePasswordAuthenticationToken(username, null, authorities);
    }
} 