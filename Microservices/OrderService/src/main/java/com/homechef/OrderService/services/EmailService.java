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
                    .println("Error sending email, the reason is most likely one of the following 2 reasons : " + '\n' +
                            "1 - you do not have the (appication-secrets.yml) file, please contact (Mohammed Tamaa) team \n "
                            +
                            "2 - the email :" + toEmail + " does not exist. \n" +
                            "error : " + e.getMessage());

        }

    }
}
