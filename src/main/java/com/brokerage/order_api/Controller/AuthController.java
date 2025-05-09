package com.brokerage.order_api.Controller;

import com.brokerage.order_api.dto.LoginRequest;
import com.brokerage.order_api.model.Customer;
import com.brokerage.order_api.repository.CustomerRepository;
import com.brokerage.order_api.security.JwtUtil;
import jakarta.security.auth.message.AuthException;
import lombok.RequiredArgsConstructor;
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
    public String login(@RequestBody LoginRequest request){
        Customer customer = customerRepository.findByUserName(request.getUsername());

        if (customer == null || !passwordEncoder.matches(request.getPassword(), customer.getPassword()))
            throw new RuntimeException("Invalid Credentials");

        return jwtUtil.generateToken(customer.getUserName(), customer.getRole());
    }

}
