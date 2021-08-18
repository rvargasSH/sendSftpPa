package sainthonore.api.sendFtp.controller;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sainthonore.api.sendFtp.repository.ProductRepository;
import sainthonore.api.sendFtp.repository.SellRepository;
import sainthonore.api.sendFtp.util.execeptions.FTPErrors;
import sainthonore.api.sendFtp.util.ftpclient.FTPService;

@RestController
@RequestMapping("send-ftp")
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

    @RequestMapping(value = "sells", method = RequestMethod.GET)
    public String sendSellsFile()
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, ParseException {
        String response = sellRepository.getSells();
        SendFtpFile("sells.txt");
        return "total records " + response;
    }

    @RequestMapping(value = "products", method = RequestMethod.GET)
    public String sendProductsFile()
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, ParseException {
        String response = productRepository.getProducts();
        SendFtpFile("products.txt");
        return "total records " + response;
    }

    public void SendFtpFile(String file) {
        try {

            ftpService.connectToFTP(sftpHost, sftpUser, sftpPass);
            ftpService.uploadFileToFTP(new File("./ftpfiles/" + file), sftpPath, file);
        } catch (FTPErrors ftpErrors) {
            System.out.println(ftpErrors.getMessage());
        }

    }
}