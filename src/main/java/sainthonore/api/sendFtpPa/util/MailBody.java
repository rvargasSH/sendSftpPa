package sainthonore.api.sendFtpPa.util;

import java.net.UnknownHostException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MailBody {

    public String sellServiceMessage()
            throws UnknownHostException {
        String mailbody = "Buen día<br><br>";
        mailbody += "Adjunto encontrara el archivo de las ventas de los servicios durante los últimos 180 días.<b><br><br>";
        mailbody += "Atentamente.";
        return mailbody;
    }

}