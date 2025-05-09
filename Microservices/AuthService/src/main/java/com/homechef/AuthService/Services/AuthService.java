package com.homechef.AuthService.Services;

import com.homechef.AuthService.Models.User;
import com.homechef.AuthService.Repositories.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final JwtService jwtService = JwtService.getInstance();

    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository , PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public String login(User user) {
        User foundUser = userRepository.findByUsername(user.getUsername());
        if (foundUser == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }
        if (!passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid password"
            );
        }
        return generateUserToken(foundUser);
    }

    public String generateUserToken(User user) {
        Map<String, Object> claims = Map.of(
                "id", user.getId(),
                "username", user.getUsername(),
                "email", user.getEmail(),
                "address", user.getAddress(),
                "phoneNumber", user.getPhoneNumber(),
                "role", user.getRole()
        );
        return jwtService.createToken(claims, user.getUsername());
    }

    public boolean validateToken(String token) {
        try {
            jwtService.validateToken(token);
            return true;
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid token"
            );
        }
    }

    public String checkAllUserFieldsPresent(User user) {
        if (user.getUsername() == null || user.getUsername().isEmpty()) {
            return "Username is required";
        }
        if (user.getPassword() == null || user.getPassword().isEmpty()) {
            return "Password is required";
        }
        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            return "Email is required";
        }
        if (user.getAddress() == null || user.getAddress().isEmpty()) {
            return "Address is required";
        }
        if (user.getPhoneNumber() == null || user.getPhoneNumber().isEmpty()) {
            return "Phone number is required";
        }
        return "g";
    }

    public String registerUser(User user, String role) {
        String check = checkAllUserFieldsPresent(user);
        if (!check.equals("g")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    check
            );
        }

        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Username already exists"
            );

        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email already exists"
            );

        }
        // check if valid email format
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid email format"
            );
        }

        // hash password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // set role unverified
        if (role.equals("customer")) {
            user.setRoleUnverifiedUser();
        } else {
            user.setRoleUnverifiedSeller();
        }
        User newUser = userRepository.save(user);

        // verify email by sending url with user.getId() and user.getEmail() to the email
        sendEmailVerificationLink(user.getEmail(), newUser.getId());
        return "Verification Email Sent";
    }

    public String sendEmailVerificationLink(String email, UUID id) {
        //TODO: send email verification link, the link should be the url of verifyEmail controller with value of id
        return "TODO";
    }

    public String verifyEmail(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }

        if (user.getRole().equals("unverified_user")) {
            user.setRoleCustomer();
        }
        else {
            user.setRoleSeller();
        }

        userRepository.save(user);
        return "Email Verified";
    }

    public String emailResetPassword(String email) {
        // TODO:
        // generate otp and store in redis along with email
        // send email with reset link (/reset-password)
        return "TODO";
    }

    public String resetPassword(String email, String otp, String newPassword) {
        // TODO:
        // check if otp is valid (compare with redis entry)
        // check if email is valid
        // check if password is valid
        // hash password
        // update password in database
        // remove otp entry from redis
        return "TODO";
    }

    public String deleteAccount(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "User not found";
        }
        userRepository.delete(user);
        return "User deleted";
    }
}
