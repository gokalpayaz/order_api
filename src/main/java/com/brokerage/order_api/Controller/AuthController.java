package com.brokerage.order_api.controller;

import com.brokerage.order_api.dto.ApiResponse;
import com.brokerage.order_api.dto.LoginRequest;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.repository.CustomerRepository;
import com.brokerage.order_api.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtUtil jwtUtil;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<String>> login(@RequestBody LoginRequest request){
        Customer customer = customerRepository.findByUserName(request.getUsername());

        if (customer == null || !passwordEncoder.matches(request.getPassword(), customer.getPassword()))
            return ResponseEntity.status(401).body(ApiResponse.<String>builder()
                    .success(false)
                    .message("Invalid username or password")
                    .data(null)
                    .build());

        String token =  jwtUtil.generateToken(customer.getUserName(), customer.getRole());
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .message("Login Successful")
                .data(token)
                .build());
    }

}
