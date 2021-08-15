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
public class ProductRepository {
    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    StorageFile saveFtpFile;

    public String getProducts() throws NoSuchAlgorithmException, NoSuchProviderException, ParseException {

        String sql = "delete [FlutePlayer].[dbo].[tmp_producto_intel_pty]";
        jdbcTemplate.update(sql);

        sql = "insert into  [FlutePlayer].[dbo].[tmp_producto_intel_pty]" + " SELECT distinct T3.Referencia+';'+"
                + "       T3.Descripcion+';'+" + "       T3.DescDept+';'+" + "       T3.DescSubDep+';'+"
                + "       T3.Referencia+';'+" + "       T3.StoreCode" + " from [FlutePlayer].[dbo].[transactions] T1, "
                + "     [FlutePlayer].[dbo].[transactionsdetails] T2," + "     [FlutePlayer].[dbo].[Articulos] T3"
                + " where T1.Invc_Sid = T2.Invc_Sid and T2.Itemcode = T3.Referencia"
                + " and T1.Invc_Date >= dateadd(day,-15,GETDATE())";
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
        saveFtpFile.CreateFile(bodyFtpFile, "products");

        sql = "delete [FlutePlayer].[dbo].[tmp_producto_intel_pty] where linea is null";
        jdbcTemplate.update(sql);
        return "total " + i;
    }

}
