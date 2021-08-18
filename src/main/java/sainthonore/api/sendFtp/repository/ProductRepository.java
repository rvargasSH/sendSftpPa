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

        String sql = "delete [SHCR].[dbo].[tmp_producto_intel_pty];";
        jdbcTemplate.update(sql);

        sql = "insert into  [SHCR].[dbo].[tmp_producto_intel_pty]"
                + " select  distinct convert(nvarchar(20),T3.Articulo)+';'+(select max(x.Descripcion1) from [SHCR].[dbo].[Art] x where x.Articulo =T3.articulo)+';'+"
                + "           substring(T3.Grupo,1,30)+';'+substring(T3.Familia,1,30)+';'+convert(nvarchar(20),T3.Articulo)+';'+'CR/'+convert(nvarchar(20),T1.Almacen)"
                + " From     [SHCR].[dbo].[venta] T1" + "         inner join [SHCR].[dbo].[ventad] T2 "
                + "                    ON T1.ID =T2.ID  " + " and T1.FechaEmision >= dateadd(day,-15,GETDATE()) "
                + "         inner join [SHCR].[dbo].[Art] T3 " + "                    ON T2.Articulo =T3.Articulo;";
        jdbcTemplate.update(sql);
        sql = "select * from [SHCR].[dbo].[tmp_producto_intel_pty];";
        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        String bodyFtpFile = "";
        Integer i = 0;
        for (final Map row : rows) {
            String cadenaTexto = (String) row.get("linea");
            try {
                bodyFtpFile += cadenaTexto.trim() + "\r\n";
                i++;
            } catch (Exception e) {

            }
        }
        saveFtpFile.CreateFile(bodyFtpFile, "products");

        sql = "delete [SHCR].[dbo].[tmp_producto_intel_pty] where linea is null";
        jdbcTemplate.update(sql);
        return "total " + i;
    }

}
