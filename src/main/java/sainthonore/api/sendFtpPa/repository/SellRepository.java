package sainthonore.api.sendFtpPa.repository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import sainthonore.api.sendFtpPa.model.SellModel;
import sainthonore.api.sendFtpPa.util.StorageFile;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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

    public final static Logger LOGGER = LoggerFactory.getLogger(SellRepository.class);

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
                + " 'PA'||t7.empl_id AS identificador_vendedor,"
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
                + " and i.created_date>=SYSDATE -15"
                + " group by i.invc_sid, i.created_date, "
                + " sb.sbs_no, s.SBS_NO||''||s.STORE_NO||''||s.STORE_CODE, cc.country_name, t7.RPRO_FULL_NAME,"
                + " t6.description1,i.WORKSTATION,t7.empl_id "
                + " order by i.created_date";

        final List<Map<String, Object>> sellsList = jdbcTemplate.queryForList(sells);
        String bodyFtpFile = "";
        Integer i = 0;
        String firstLine = "sbs_no_local;numero_boleta;monto;fecha;identificador_caja;identificador_vendedor;identificador_producto;cantidad_de_productos_vendidos;precio_producto_vendido;codigo_transaccion;nombre_vendedor\r\n";
        bodyFtpFile += firstLine;
        LOGGER.info("result from query" + sellsList.size());
        String fileName = saveFtpFile.createFileWithUtf8("sells-panama");
        try (FileOutputStream fos = new FileOutputStream(fileName);
                OutputStreamWriter osw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                BufferedWriter writer = new BufferedWriter(osw)) {

            writer.append(bodyFtpFile);
            for (final Map sell : sellsList) {
                String CODIGO = (String) sell.get("sbs_no_local");
                String invc_sid = sell.get("numero_boleta").toString();
                BigDecimal VentaTotalSole = (BigDecimal) sell.get("monto");
                String CREATEDAT = (String) sell.get("fecha");
                BigDecimal workstation = (BigDecimal) sell.get("identificador_caja");
                String empl_id = (String) sell.get("identificador_vendedor");
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
                writer.append(line.trim() + "\r\n");
                i++;
            }
            writer.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return "total " + i;

    }

    public List<Map<String, Object>> getServicesSells()
            throws NoSuchAlgorithmException, NoSuchProviderException, ParseException {
        String sells = "select s.SBS_NO||''||s.STORE_NO||''||s.STORE_CODE AS storeCode, i.invc_sid AS Idtransaction ,s.STORE_NAME,"
                + " TO_CHAR(sum(case when i.invc_type=0 then (ii.qty*ii.price)"
                + " when i.invc_type=2 then (ii.qty*ii.price)*-1 end ),'FM999G999G999G990D00','nls_numeric_characters=,.') AS price,"
                + " sum(case when i.invc_type=0 then ii.qty"
                + " when i.invc_type=2 then ii.qty*-1 end ) AS quantity,"
                + " TO_CHAR(i.created_date,'DD-MM-YYYY HH24:MI:SS') AS sellDate,"
                + " t7.RPRO_FULL_NAME AS seller,"
                + " t6.description1 AS productCode,"
                + " t6.description2 AS productName,"
                + " t6.description3 AS brand,"
                + " t6.attr AS family,"
                + " t6.siz AS productGroup"
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
                + " AND i.created_date>=SYSDATE -30"
                + " AND i.INVC_SID in(SELECT i.invc_sid from invoice i inner join invc_item ii on i.invc_sid=ii.invc_sid"
                + "                    inner join invn_sbs t6 on ii.item_sid = t6.item_sid and t6.sbs_no=i.sbs_no "
                + "                    where i.invc_no>0 AND i.created_date>=TO_DATE('2022-01-01','yyyy-mm-dd') "
                + "                    AND t6.description1 IN('SERVICE30','SERVICE60','LESSON30','LESSON60')"
                + "                    group by i.invc_sid)"
                + " group by i.invc_sid, i.created_date, s.store_name,  c.first_name, c.last_name, c.cust_id,"
                + " sb.sbs_no, s.SBS_NO||''||s.STORE_NO||''||s.STORE_CODE, cc.country_name, t7.RPRO_FULL_NAME,"
                + " t6.description1,i.store_no,description2,description3,attr,siz"
                + " order by i.created_date";

        final List<Map<String, Object>> sellsList = jdbcTemplate.queryForList(sells);
        return sellsList;

    }

}
