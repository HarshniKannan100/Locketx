package com.wschatapp.controller;

import com.wschatapp.model.User;
import com.wschatapp.service.UserService;
import com.wschatapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin("*")
public class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public String register(@RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");

        return userService.register(username, password);
    }

    public UserService getUserService() {
        return userService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public Object login(@RequestBody Map<String, String> body) {

        String username = body.get("username");
        String password = body.get("password");

        User user = userService.login(username, password);

        if (user != null) {
            String token = JwtUtil.generateToken(user.getId());

            return Map.of(
                    "token", token,
                    "id", user.getId(),
                    "name",user.getUsername()
            );
        }

        return "Invalid credentials";
    }
}
