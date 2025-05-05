package Controllers;

import Services.AuthService;
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

    @PutMapping("/verify-email/{userId}")
    public String verifyEmail(@PathVariable UUID userId) {
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

}
