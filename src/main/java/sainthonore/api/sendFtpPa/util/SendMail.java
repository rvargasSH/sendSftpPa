package sainthonore.api.sendFtpPa.util;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import sendinblue.ApiClient;
import sendinblue.Configuration;
import sendinblue.auth.ApiKeyAuth;
import sibApi.TransactionalEmailsApi;
import sibModel.CreateSmtpEmail;
import sibModel.SendSmtpEmail;
import sibModel.SendSmtpEmailAttachment;
import sibModel.SendSmtpEmailSender;
import sibModel.SendSmtpEmailTo;

@Component
public class SendMail {
    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${sendingblue.apiKey}")
    private String sendingBlueApiKey;

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

    public Boolean sendMailBySendingBlue(List<String> destinatary, String body, String subject, String attachPath) {

        ApiClient defaultClient = Configuration.getDefaultApiClient();
        ApiKeyAuth apiKey = (ApiKeyAuth) defaultClient.getAuthentication("api-key");
        apiKey.setApiKey(sendingBlueApiKey);
        try {
            TransactionalEmailsApi apiInstance = new TransactionalEmailsApi();
            SendSmtpEmailSender sender = new SendSmtpEmailSender();
            sender.setEmail(fromEmail);
            sender.setName("Saint Honore");
            List<SendSmtpEmailTo> toList = new ArrayList<SendSmtpEmailTo>();
            for (String toEmail : destinatary) {
                SendSmtpEmailTo to = new SendSmtpEmailTo();
                to.setEmail(toEmail);
                toList.add(to);
            }

            SendSmtpEmail sendSmtpEmail = new SendSmtpEmail();
            sendSmtpEmail.setSender(sender);
            sendSmtpEmail.setTo(toList);
            sendSmtpEmail.setHtmlContent(body);
            sendSmtpEmail.setSubject(subject);
            if (attachPath != null) {
                File file = new File(attachPath);
                byte[] bytes = FileUtils.readFileToByteArray(file);
                SendSmtpEmailAttachment attachFile = new SendSmtpEmailAttachment();
                attachFile.content(bytes);
                attachFile.setName(file.getName());
                List<SendSmtpEmailAttachment> attachments = new ArrayList<>();
                attachments.add(attachFile);
                sendSmtpEmail.setAttachment(attachments);
            }

            CreateSmtpEmail response = apiInstance.sendTransacEmail(sendSmtpEmail);
            System.out.println("RESPONSE" + response.toString());
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
}