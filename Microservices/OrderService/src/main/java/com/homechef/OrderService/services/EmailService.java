package com.homechef.OrderService.services;

// 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendEmail(String toEmail, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);

        // while testing, the dummy data will most likely
        // contain fake emails, so we dont want to throw an error
        // in that case
        try {
            mailSender.send(message);
            System.out
                    .println("Email sent successfully to " + toEmail + " with subject: " + subject + " and body: "
                            + body);
        } catch (MailException e) {
            System.out
                    .println("Error sending email, does the mail : " + toEmail + " really exist? \n" + e.getMessage());
        }

    }
}
