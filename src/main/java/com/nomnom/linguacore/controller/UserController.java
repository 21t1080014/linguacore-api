package com.nomnom.linguacore.controller;

import com.nomnom.linguacore.dto.request.LoginRequest;
import com.nomnom.linguacore.dto.request.RegisterRequest;
import com.nomnom.linguacore.dto.response.LoginResponse;
import com.nomnom.linguacore.dto.response.UserResponse;
import com.nomnom.linguacore.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request){
        return ResponseEntity.ok(userService.login(request));
    }
}
