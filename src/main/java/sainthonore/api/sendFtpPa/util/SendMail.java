package sainthonore.api.sendFtpPa.util;

import java.io.File;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class SendMail {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void singleAddress(String address, String subject, String messagebody) throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();

        message.setSubject(subject);
        MimeMessageHelper helper;
        helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(address);
        helper.setText(messagebody, true);
        javaMailSender.send(message);

    }

    public void singleAddressWithAttach(String[] address, String subject, String messagebody, String attachFile)
            throws MessagingException {

        MimeMessage message = javaMailSender.createMimeMessage();

        message.setSubject(subject);
        MimeMessageHelper helper;
        helper = new MimeMessageHelper(message, true);
        helper.setFrom(fromEmail);
        helper.setTo(address);
        helper.setText(messagebody, true);
        FileSystemResource file = new FileSystemResource(new File(attachFile));
        helper.addAttachment(attachFile, file);
        javaMailSender.send(message);

    }
}