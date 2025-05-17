package com.homechef.AuthService.Services;

import com.homechef.AuthService.Models.User;
import com.homechef.AuthService.Repositories.UserRepository;


import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.security.SecureRandom;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import java.util.Map;
import java.util.stream.Collectors;


@Service
public class AuthService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;

    private final JwtService jwtService = JwtService.getInstance();

    private PasswordEncoder passwordEncoder;



    private final StringRedisTemplate redisTemplate;


    private AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender, StringRedisTemplate redisTemplate, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate; // Inject Redis template
        this.authenticationManager = authenticationManager;
    }

    public String login(User user) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
        try{
            if (authenticate.isAuthenticated()) {
                // check if verified
                User foundUser = userRepository.findByUsername(user.getUsername()).get();
                if (foundUser.getRole().equals("unverified_user") || foundUser.getRole().equals("unverified_seller")) {
                    throw new ResponseStatusException(
                            HttpStatus.UNAUTHORIZED,
                            "User not verified using email"
                    );
                }

                return generateUserToken(user);
            } else {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid username or password"
                );
            }
        } catch (Exception e) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid username or password"
            );
        }

    }

    public String generateUserToken(User user) {
        User foundUser = userRepository.findByUsername(user.getUsername()).get();
        Map<String, Object> claims = Map.of(
                "id", foundUser.getId(),
                "username", foundUser.getUsername(),
                "email", foundUser.getEmail(),
                "address", foundUser.getAddress(),
                "phoneNumber", foundUser.getPhoneNumber(),
                "role", foundUser.getRole()
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
        return "good";
    }

    public String registerUser(User user, String role) {
        String check = checkAllUserFieldsPresent(user);
        if (!check.equals("good")) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    check
            );
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
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


    //NEEDS TESTING
    public String sendEmailVerificationLink(String email, UUID id) {
        //TODONE: send email verification link, the link should be the url of verifyEmail controller with value of id`

        // Construct the verification link URL
        String verifyEmailUrl = "http://localhost:8081/auth/verify-email?userId=" + id;

        // Compose the email using SimpleMailMessage
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Verify Your Email");
        message.setText("Please click the following link to verify your email: " + verifyEmailUrl);
        message.setFrom("no-reply@yourapp.com"); // Replace with your sender address

        // Send the email
        try {
            mailSender.send(message);
            return "Verification email sent to " + email;
        } catch (Exception e) {
            e.printStackTrace();
            return "Failed to send verification email";
        }
    }


    public String emailResetPassword(String email) {
        // TODO:
        // generate otp and store in redis along with email
        // send email with reset link (/reset-password)

        // Check if the user exists
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "No account associated with this email.";
        }

        // Generate a 6-digit OTP
        String otp = generateOTP(6);

        // Save OTP in Redis with an expiration time (5 minutes)
        saveOtpToRedis(email, otp, 300000); // 300000ms = 5 minutes

        // Send OTP via email to the user
        sendOtpEmail(email, otp);

        return "OTP has been sent to your registered email address.";
    }



    public String resetPassword(String email, String otp, String newPassword) {
        // Check if the user is blocked
        if (isBlocked(email)) {
            return "Too many invalid attempts. Please try again later.";
        }

        // Validate the OTP
        boolean isOtpValid = validateOtp(email, otp);

        if (!isOtpValid) {
            incrementFailedAttempts(email); // Track failed attempts
            return "Invalid or expired OTP. Please request a new one.";
        }

        // Reset attempts after successful validation
        resetAttempts(email);

        // Update the user password
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User not found with the provided email.";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        return "Password has been successfully updated.";
    }



    //OTP Generation Method
    public String generateOTP(int length) {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();

        for (int i = 0; i < length; i++) {
            otp.append(random.nextInt(10)); // Generates a random number (0-9)
        }
        return otp.toString();
    }

    //Storing OTP in Redis
    public void saveOtpToRedis(String email, String otp, long expirationMillis) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        valueOps.set(email, otp, expirationMillis, TimeUnit.MILLISECONDS); // Save in Redis with expiration
    }

    //OTP Validation Method
    public boolean validateOtp(String email, String userInputOtp) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String storedOtp = valueOps.get(email);

        if (storedOtp == null) {
            return false; // OTP expired or does not exist
        }

        if (storedOtp.equals(userInputOtp)) {
            redisTemplate.delete(email); // Remove OTP after successful validation
            return true;
        }
        return false; // OTP does not match
    }

    //sending OTP via email

    public void sendOtpEmail(String email, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP code is: " + otp + ". It is valid for 5 minutes. " +
                "Do not share this code with anyone.");

        mailSender.send(message);
    }

    public boolean isBlocked(String email) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String attempts = valueOps.get(email + ":attempts");
        return attempts != null && Integer.parseInt(attempts) >= 3;
    }

    public void incrementFailedAttempts(String email) {
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String key = email + ":attempts";
        String attempts = valueOps.get(key);

        if (attempts == null) {
            valueOps.set(key, "1", 600, TimeUnit.SECONDS); // 10-minute block duration
        } else {
            valueOps.increment(key);
        }
    }


    public void resetAttempts(String email) {
        redisTemplate.delete(email + ":attempts");
    }

    public String deleteAccount(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }
        userRepository.delete(user);
        return "User deleted";
    }


    public Map<String, String> getUsersEmails(List<UUID> ids) {
        List<User> users = userRepository.findAllById(ids);
        if (users.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "No users found"
            );
        }
        return users.stream()
                .collect(Collectors.toMap(
                        user -> user.getId().toString(),  // Convert UUID (or any ID) to String
                        User::getEmail
                ));
    }

}
