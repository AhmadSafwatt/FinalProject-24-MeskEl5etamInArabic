package Services;

import Models.User;
import Repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;


import java.util.UUID;

@Service
public class AuthService {
    private final JavaMailSender mailSender;

    private final UserRepository userRepository;


    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender; // Initialize mailSender
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



    public String sendEmailVerificationLink(String email, UUID id) {
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
}
