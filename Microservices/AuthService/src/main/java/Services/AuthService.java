package Services;

import Models.User;
import Repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


import java.util.UUID;

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
    //STRINGREDISTEMPLATE PROBLEM SOLVED

    public String emailResetPassword(String email) {
        // TODO:
        // generate otp and store in redis along with email
        // send email with reset link (/reset-password)

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

    }
}
