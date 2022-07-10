package sainthonore.api.sendFtpPa.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import sainthonore.api.sendFtpPa.model.SellModel;
import sainthonore.api.sendFtpPa.util.StorageFile;

import java.math.BigDecimal;
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

    public List<String> getSubsidiaries() {
        List<String> subsidiariesR = new ArrayList<>();
        String subsidiaries = "SELECT sbs_no FROM subsidiary WHERE SBS_NO>=1 ORDER BY SBS_NO";
        final List<Map<String, Object>> listSubsidiaries = jdbcTemplate.queryForList(subsidiaries);
        for (final Map subsidiary : listSubsidiaries) {
            BigDecimal sbs_no = (BigDecimal) subsidiary.get("sbs_no");
            subsidiariesR.add(sbs_no.toString());
        }
        return subsidiariesR;
    }

    public List<String> getStoreTypes(String sbsNo) {
        List<String> storesTypesR = new ArrayList<>();
        String storesTypes = "SELECT i.store_no from invoice i where i.sbs_no=" + sbsNo + ""
                + " and i.invc_no>0 and  i.created_date>=SYSDATE -15"
                + " group by i.store_no";
        final List<Map<String, Object>> listStores = jdbcTemplate.queryForList(storesTypes);
        for (final Map storeType : listStores) {
            BigDecimal store_no = (BigDecimal) storeType.get("store_no");
            storesTypesR.add(store_no.toString());
        }
        return storesTypesR;
    }

    public String getSells()
            throws NoSuchAlgorithmException, NoSuchProviderException, ParseException {
        String sells = "select s.SBS_NO||''||s.STORE_NO||''||s.STORE_CODE AS sbs_no_local, i.invc_sid AS numero_boleta,"
                + " sum(case when i.invc_type=0 then (ii.qty*ii.price)"
                + " when i.invc_type=2 then (ii.qty*ii.price)*-1 end ) AS monto,"
                + " TO_CHAR(i.created_date,'DD-MM-YYYY HH24:MI:SS') AS fecha,"
                + " i.WORKSTATION as identificador_caja,"
                + " t7.empl_id AS identificador_vendedor,"
                + "  t6.description1 AS identificador_producto,"
                + " sum(case when i.invc_type=0 then ii.qty"
                + " when i.invc_type=2 then ii.qty*-1 end ) AS cantidad_de_productos_vendidos,"
                + " min(case when i.invc_type=0 then 'Regular' when i.invc_type=2 then 'Return' end) movimiento,"
                + " t7.RPRO_FULL_NAME"
                + " from invoice i"
                + " inner join invc_item ii on i.invc_sid=ii.invc_sid"
                + " inner join invn_sbs t6 on ii.item_sid = t6.item_sid and t6.sbs_no=i.sbs_no"
                + " inner join employee t7 on ii.clerk_id = t7.empl_id and t7.sbs_no=i.sbs_no"
                + " inner join store s on (s.store_no=i.store_no and s.sbs_no=i.sbs_no)"
                + " left join customer c on (c.cust_sid=i.cust_sid)"
                + " left join cust_address ca on (ca.cust_sid=c.cust_sid and ca.addr_no=1 and c.cust_type=0)"
                + " left join country cc on (cc.country_id=ca.country_id)"
                + " inner join subsidiary sb on (sb.sbs_no=i.sbs_no)"
                + " where i.invc_no>0"
                + " and  i.created_date>=SYSDATE -15"
                + " group by i.invc_sid, i.created_date, "
                + " sb.sbs_no, s.SBS_NO||''||s.STORE_NO||''||s.STORE_CODE, cc.country_name, t7.RPRO_FULL_NAME,"
                + " t6.description1,i.WORKSTATION,t7.empl_id "
                + " order by i.created_date";

        final List<Map<String, Object>> sellsList = jdbcTemplate.queryForList(sells);
        String bodyFtpFile = "";
        Integer i = 0;
        String firstLine = "sbs_no_local;numero_boleta;monto;fecha;identificador_caja;identificador_vendedor;identificador_producto;cantidad_de_productos_vendidos;precio_producto_vendido;codigo_transaccion;nombre_vendedor\r\n";
        bodyFtpFile += firstLine;
        for (final Map sell : sellsList) {
            String CODIGO = (String) sell.get("sbs_no_local");
            String invc_sid = sell.get("numero_boleta").toString();
            BigDecimal VentaTotalSole = (BigDecimal) sell.get("monto");
            String CREATEDAT = (String) sell.get("fecha");
            BigDecimal workstation = (BigDecimal) sell.get("identificador_caja");
            BigDecimal empl_id = (BigDecimal) sell.get("identificador_vendedor");
            String ITEM_SID = sell.get("identificador_producto").toString();
            BigDecimal Qty = (BigDecimal) sell.get("cantidad_de_productos_vendidos");
            String movimiento = (String) sell.get("movimiento");
            String vendedor = (String) sell.get("RPRO_FULL_NAME");
            Float individualSell = Float.parseFloat(VentaTotalSole.toString()) / Float.parseFloat(Qty.toString());
            String line = CODIGO + ";" + invc_sid + ";" + individualSell +
                    ";" + CREATEDAT + ";"
                    + workstation + ";" + empl_id + ";" + ITEM_SID + ";" + Qty + ";" +
                    individualSell + ";"
                    + movimiento + ";" + vendedor;
            bodyFtpFile += line.trim() + "\r\n";
            i++;
        }
        saveFtpFile.CreateFile(bodyFtpFile, "sells");
        return "total " + i;

    }

}
