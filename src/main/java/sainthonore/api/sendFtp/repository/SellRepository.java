package sainthonore.api.sendFtp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import sainthonore.api.sendFtp.model.SellModel;
import sainthonore.api.sendFtp.util.StorageFile;

import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.text.ParseException;

@Service
public class SellRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    StorageFile saveFtpFile;

    public String getSells() throws NoSuchAlgorithmException, NoSuchProviderException, ParseException {

        String sql = "delete [FlutePlayer].[dbo].[tmp_producto_intel_pty]";
        jdbcTemplate.update(sql);

        sql = "insert into  [FlutePlayer].[dbo].[tmp_producto_intel_pty]" + "SELECT T2.whscode+';'+"
                + "       convert(varchar(10),T1.InvcNo)+';'+"
                + "       (case when T1.TransType = 'F' then convert(varchar(20),T2.Quantity * isnull(T2.Price,0)) else convert(varchar(20),T2.Quantity * isnull(T2.Price,0)*(-1)) end)+';'+"
                + "       convert(varchar(10),T1.Invc_Date,101)+' '+convert(varchar(8),T1.Invc_Date,108)+';'+"
                + "       '0'+';'+"
                + "       isnull((select max(x.nombre)+' '+max(x.apellido) from [FlutePlayer].[dbo].[Empleados] x "
                + "                   where x.SlpCode = T2.Slpcode),'UNKNOW') +';'+" + "       T2.ItemCode+';'+"
                + "       (case when T1.TransType = 'F' then convert(varchar(20),T2.Quantity) else convert(varchar(20),T2.Quantity*(-1)) end) +';'+"
                + "       convert(varchar(20),isnull(T2.Price,0)) +';'+"
                + "      (case when T1.TransType = 'F' then 'FACTURA' ELSE 'NOTA DE CR' END)"
                + " from [FlutePlayer].[dbo].[transactions] T1, [FlutePlayer].[dbo].[transactionsdetails] T2"
                + " where T1.Invc_Sid = T2.Invc_Sid" + " and T1.Invc_Date >= dateadd(day,-15,GETDATE())";
        jdbcTemplate.update(sql);
        sql = "select  linea from [FlutePlayer].[dbo].[tmp_producto_intel_pty];";

        final List<SellModel> eventosliquidados = new ArrayList<>();

        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        String bodyFtpFile = "";
        Integer i = 0;
        for (final Map row : rows) {
            String cadenaTexto = (String) row.get("linea");
            bodyFtpFile += cadenaTexto.trim() + "\r\n";
            i++;

        }
        saveFtpFile.CreateFile(bodyFtpFile, "sells");
        return "total " + i;
    }

}
