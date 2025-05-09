package com.homechef.AuthService.Controllers;

import com.homechef.AuthService.Models.User;
import com.homechef.AuthService.Services.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping
    public String test() {
        return "Works!";
    }
    @PostMapping("/register")
    public String registerUser(@RequestBody User requestBody) {
        return authService.registerUser(requestBody, requestBody.getRole());
    }

    @PostMapping("/token")
    public String login(@RequestBody User requestBody) {
        return authService.login(requestBody);
    }

    @GetMapping("/validate-token")
    public String validateToken(@RequestParam String token) {
        return authService.validateToken(token) ? "Valid token" : "Invalid token";
    }

    @PutMapping("/verify-email")
    public String verifyEmail(@RequestParam UUID userId) {
        return authService.verifyEmail(userId);
    }


    @PutMapping("/reset-password")
    public String resetPassword(@RequestParam String email) {
        return authService.emailResetPassword(email);
    }


    @PutMapping("/update-password")
    public String updatePassword(@RequestBody Map<String, String> requestBody) {
        return authService.resetPassword(requestBody.get("email"), requestBody.get("otp"), requestBody.get("newPassword"));
    }

    // @PROTECTED
    @DeleteMapping("/delete-account/{userId}")
    public String deleteAccount(@PathVariable UUID userId) {
        return authService.deleteAccount(userId);
    }

}
