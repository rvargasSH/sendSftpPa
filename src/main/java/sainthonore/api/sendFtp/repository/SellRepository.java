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

        String sql = "delete [SHCR].[dbo].[tmp_producto_intel_pty]";
        jdbcTemplate.update(sql);

        sql = "insert into  [SHCR].[dbo].[tmp_producto_intel_pty]" + " select  convert(nvarchar(20),T1.Almacen)+';'+"
                + "                 convert(varchar(20),T1.ID)+';'+"
                + "(case when T1.mov = 'Devolucion Mostrador' then "
                + "                          convert(varchar(20),((-1)*isnull(T2.Cantidad,0) * isnull(T2.precio,0)) - ((-1)*isnull(T2.Cantidad,0) * isnull(T2.precio,0)*isnull(T2.Descuentolinea,0)/100)) else"
                + "                          convert(varchar(20),(isnull(T2.Cantidad,0) * isnull(T2.precio,0)) - (isnull(T2.Cantidad,0) * isnull(T2.precio,0)*isnull(T2.Descuentolinea,0)/100)) end) +';'+"
                + "                 convert(varchar(10),T1.FechaEmision,101)+' '+convert(varchar(8),T1.FechaEmision,108)+';'+"
                + "                 '0'+';'+"
                + "                 isnull((select x.nombre from [SHCR].[dbo].[Agente] x where x.Agente = T1.Agente),'UNKNOW') +';'+"
                + "                 convert(nvarchar(20),T3.Articulo)+';'+"
                + "                 (case when T1.mov = 'Devolucion Mostrador' then "
                + "                      convert(varchar(20),(-1)*T2.Cantidad) else"
                + "                      convert(varchar(20),T2.Cantidad) end) +';'+"
                + "                 convert(varchar(20),(((isnull(T2.Cantidad,0) * isnull(T2.precio,0)) - (isnull(T2.Cantidad,0) * isnull(T2.precio,0)*isnull(T2.Descuentolinea,0)/100))/T2.Cantidad)) +';'+"
                + "                 T1.mov" + " From     [SHCR].[dbo].[venta] T1"
                + "         inner join [SHCR].[dbo].[ventaD] T2 " + "                    ON T1.ID =T2.ID "
                + "                      and T1.Almacen in ('AC-VTAS','LE-VTAS','AE-VTA','AE-VTAS','AZ-VTA','AH-VTAS')"
                + "                      and T1.FechaEmision >= dateadd(day,-15,GETDATE())"
                + "                      and T1.mov in ('Devolucion','Devolucion Est','Devolucion Interco',"
                + "                                     'Devolucion Mostrador','Factura','Factura Abono',"
                + "                                     'Factura Canje','Factura Contado','Factura Contado*',"
                + "                                     'Factura Contado_','Factura Credito','Factura Est',"
                + "                                     'Factura Interco','Factura Mostrador')"
                + "         inner join [SHCR].[dbo].[Art] T3  " + "                    ON T2.Articulo =T3.Articulo";
        jdbcTemplate.update(sql);
        sql = "select  linea from [SHCR].[dbo].[tmp_producto_intel_pty];";

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
