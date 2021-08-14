package sainthonore.api.sendFtp.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.text.ParseException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import sainthonore.api.sendFtp.model.SellModel;
import sainthonore.api.sendFtp.repository.SellRepository;

@RestController
@RequestMapping("send-ftp")
public class SendFtpController {

    @Autowired
    SellRepository sellRepository;

    @RequestMapping(value = "sells", method = RequestMethod.GET)
    public String sendSellsFile()
            throws IOException, NoSuchAlgorithmException, NoSuchProviderException, ParseException {
        List<SellModel> totalSells = sellRepository.getSells();

        return null;
    }
}