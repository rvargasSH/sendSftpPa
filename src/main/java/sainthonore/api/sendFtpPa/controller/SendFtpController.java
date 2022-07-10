package sainthonore.api.sendFtpPa.controller;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sainthonore.api.sendFtpPa.repository.ProductRepository;
import sainthonore.api.sendFtpPa.repository.SellRepository;
import sainthonore.api.sendFtpPa.util.execeptions.FTPErrors;
import sainthonore.api.sendFtpPa.util.ftpclient.FTPService;

@RestController
@RequestMapping("send-ftp")
@EnableScheduling
public class SendFtpController {

    @Autowired
    SellRepository sellRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private FTPService ftpService;

    @Value("${sftp.host}")
    private String sftpHost;

    @Value("${sftp.user}")
    private String sftpUser;

    @Value("${sftp.password}")
    private String sftpPass;

    @Value("${sftp.path}")
    private String sftpPath;

    @Scheduled(cron = "00 0/20 * * * *")
    @RequestMapping(value = "sells", method = RequestMethod.GET)
    public String sendSellsFile()
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, ParseException {

        String response = "";
        Format formatToDate = new SimpleDateFormat("yyMMdd");
        response = sellRepository.getSells();
        System.out.println("here is sending");
        SendFtpFile("sells.txt", "/Panama/Ventas/",
                "VENTAS" + formatToDate.format(new Date()) + ".txt");
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
}