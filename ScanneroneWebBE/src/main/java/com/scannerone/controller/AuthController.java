package com.scannerone.controller;

import com.scannerone.dto.RegistrationResponseDto;
import com.scannerone.entity.User;
import com.scannerone.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<RegistrationResponseDto> register() {
        User user = userService.registerUser();
        return ResponseEntity.ok(new RegistrationResponseDto(user.getDeviceToken(), user.getPassword()));
    }
}