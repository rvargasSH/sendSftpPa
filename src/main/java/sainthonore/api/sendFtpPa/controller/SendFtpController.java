package sainthonore.api.sendFtpPa.controller;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sainthonore.api.sendFtpPa.repository.ProductRepository;
import sainthonore.api.sendFtpPa.repository.SellRepository;
import sainthonore.api.sendFtpPa.util.CreateExcel;
import sainthonore.api.sendFtpPa.util.MailBody;
import sainthonore.api.sendFtpPa.util.SendMail;
import sainthonore.api.sendFtpPa.util.execeptions.FTPErrors;
import sainthonore.api.sendFtpPa.util.ftpclient.FTPService;

@RestController
@RequestMapping("send-ftp")
// @EnableScheduling
public class SendFtpController {

    @Autowired
    SellRepository sellRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CreateExcel createExcel;

    @Autowired
    SendMail sendMail;

    @Autowired
    private FTPService ftpService;

    @Autowired
    MailBody mailbody;

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.user}")
    private String sftpUser;

    @Value("${sftp.password}")
    private String sftpPass;

    @Value("${sftp.path}")
    private String sftpPath;

    public final static Logger LOGGER = LoggerFactory.getLogger(SendFtpController.class);

    @Scheduled(cron = "00 0/20 * * * *")
    @RequestMapping(value = "sells", method = RequestMethod.GET)
    public String sendSellsFile()
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, ParseException {

        String response = "";
        Format formatToDate = new SimpleDateFormat("yyMMdd");
        response = sellRepository.getSells();
        System.out.println("here is sending");
        LOGGER.info("here is sending");
        SendFtpFile("sells.txt", "/upload/",
                "VENTAS" + formatToDate.format(new Date()) + "PA.txt");
        LOGGER.info("filesend-totalrecords" + response);
        return "total records " + response;
    }

    @RequestMapping(value = "products", method = RequestMethod.GET)
    public String sendProductsFile()
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, ParseException {
        String response = productRepository.getProducts();
        // SendFtpFile("products.txt");
        return "total records " + response;
    }

    public void SendFtpFile(String file, String destinyDirectory, String finalName) {
        try {

            ftpService.connectToFTP(sftpHost, sftpUser, sftpPass);
            ftpService.uploadFileToFTP(new File("./ftpfiles/" + file), destinyDirectory, finalName);
        } catch (FTPErrors ftpErrors) {
            System.out.println(ftpErrors.getMessage());
        }

    }

    @Scheduled(cron = "00 00 04 /16 * *")
    @RequestMapping(value = "services-sells", method = RequestMethod.GET)
    public String sendServicesSellsByMail()
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, ParseException, MessagingException {

        List<Map<String, Object>> data = sellRepository.getServicesSells();
        String filetosend = createExcel.generateSellsExport(data);
        String mailBody = mailbody.sellServiceMessage();
        String[] mailAddress = new String[2];
        mailAddress[0] = "ana.chang@sthonore.com.pa";
        mailAddress[1] = "vargas.reynaldo@sthonore.com.co";
        sendMail.singleAddressWithAttach(mailAddress, "Service sells", mailBody, filetosend);

        return "ok";
    }

}