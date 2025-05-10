package com.homechef.AuthService.Services;

import com.homechef.AuthService.Models.User;
import com.homechef.AuthService.Repositories.UserRepository;

<<<<<<< HEAD
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.core.Authentication;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;

    private final JwtService jwtService = JwtService.getInstance();

    private PasswordEncoder passwordEncoder;


    private AuthenticationManager authenticationManager;

    @Autowired
    public AuthService(UserRepository userRepository , PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }

    public String login(User user) {
        Authentication authenticate = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));
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
=======
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class AuthService {
    private final JavaMailSender mailSender;
    private final UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private final StringRedisTemplate redisTemplate;




    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender, StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate; // Inject Redis template
    }



    public String registerUser(User user, String role) {
        if (userRepository.findByUsername(user.getUsername()) != null) {
            return "Username already exists";
        }
        if (userRepository.findByEmail(user.getEmail()) != null) {
            return "Email already exists";
        }
        // check if valid email format
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            return "Invalid email format";
>>>>>>> origin/Youssef_Alaa
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

<<<<<<< HEAD
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
=======

    //NEEDS TESTING
    public String sendEmailVerificationLink(String email, UUID id) {
        //TODONE: send email verification link, the link should be the url of verifyEmail controller with value of id`

        // Construct the verification link URL
        String verifyEmailUrl = "http://localhost:8081/auth/verify-email/" + id;

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
    public String verifyEmail(UUID id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return "User not found";
>>>>>>> origin/Youssef_Alaa
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
<<<<<<< HEAD
=======
    //STRINGREDISTEMPLATE PROBLEM SOLVED
>>>>>>> origin/Youssef_Alaa

    public String emailResetPassword(String email) {
        // TODO:
        // generate otp and store in redis along with email
        // send email with reset link (/reset-password)
<<<<<<< HEAD
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
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "User not found"
            );
        }
        userRepository.delete(user);
        return "User deleted";
=======

        // 1. Check if the user exists in the system
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User with this email does not exist.";
        }

        // 2. Generate a one-time password (OTP) or token
        String otp = UUID.randomUUID().toString();

        // 3. Store the OTP in Redis with an expiration time (e.g., 10 minutes)
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        valueOps.set("RESET_PASSWORD_" + email, otp, 10, TimeUnit.MINUTES);

        // 4. Send the OTP to the user's email
        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(email);
        mailMessage.setSubject("Reset Your Password");
        mailMessage.setText("Use the following OTP to reset your password: " + otp +
                "\nNote: This OTP will expire in 10 minutes.");

        try {
            mailSender.send(mailMessage);
        } catch (Exception e) {
            // Log any errors that occur during email sending (if you have a logger)
            System.err.println("Error sending password reset email: " + e.getMessage());
            return "Failed to send reset password email. Please try again later.";
        }

        // 5. Return success message
        return "Password reset instructions have been sent to the email.";

    }
    //STRINGREDISTEMPLATE PROBLEM SOLVED

    public String resetPassword(String email, String otp, String newPassword) {
        // 1. Check if a user exists with the provided email
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return "User with this email does not exist.";
        }

        // 2. Retrieve OTP from Redis and validate
        ValueOperations<String, String> valueOps = redisTemplate.opsForValue();
        String storedOtp = valueOps.get("RESET_PASSWORD_" + email);

        if (storedOtp == null) {
            return "OTP has expired or is invalid.";
        }

        if (!storedOtp.equals(otp)) {
            return "Invalid OTP. Please try again.";
        }

        // 3. Encrypt the new password and update the user
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(encodedPassword);
        userRepository.save(user);

        // 4. Remove OTP from Redis after successful password reset
        redisTemplate.delete("RESET_PASSWORD_" + email);

        // 5. Return success message
        return "Password has been successfully reset.";

>>>>>>> origin/Youssef_Alaa
    }
}
