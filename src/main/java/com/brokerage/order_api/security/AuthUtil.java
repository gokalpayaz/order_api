package com.brokerage.order_api.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.brokerage.order_api.model.Customer;

public class AuthUtil {
    public static boolean isAuthorizedForCustomer(Customer customer) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_admin"));
        return isAdmin || customer.getUsername().equals(username);
    }
} 