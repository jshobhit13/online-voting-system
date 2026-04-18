package com.shobhit.voting_system.controller;

import com.shobhit.voting_system.entity.User;
import com.shobhit.voting_system.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/register")
    public User register(@RequestBody User user) {
        return userRepository.save(user);
    }
}