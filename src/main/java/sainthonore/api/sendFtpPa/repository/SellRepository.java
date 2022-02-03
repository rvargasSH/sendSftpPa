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
                + " and i.invc_no>0 and  i.created_date between to_date('01/01/2022 00:00:00', 'DD/MM/YYYY HH24:MI:SS')"
                + " and  to_date('31/01/2022 23:59:59', 'DD/MM/YYYY HH24:MI:SS')"
                + " group by i.store_no";
        final List<Map<String, Object>> listStores = jdbcTemplate.queryForList(storesTypes);
        for (final Map storeType : listStores) {
            BigDecimal store_no = (BigDecimal) storeType.get("store_no");
            storesTypesR.add(store_no.toString());
        }
        return storesTypesR;
    }

    public String getSells(String sbsNo, String store_no)
            throws NoSuchAlgorithmException, NoSuchProviderException, ParseException {
        String sells = "select s.SBS_NO||''||s.STORE_NO||''||s.STORE_CODE||';'|| i.invc_sid ||';'||"
                + " TO_CHAR(sum(case when i.invc_type=0 then (ii.qty*ii.price)"
                + " when i.invc_type=2 then (ii.qty*ii.price)*-1 end ),'FM999G999G999G990D00','nls_numeric_characters=,.')||';'||"
                + " sum(case when i.invc_type=0 then ii.qty"
                + " when i.invc_type=2 then ii.qty*-1 end )||';'||"
                + " TO_CHAR(i.created_date,'DD-MM-YYYY HH24:MI:SS')||';'||0||';'||"
                + " t7.RPRO_FULL_NAME||';'||"
                + " t6.description1||';'||"
                + " min(case when i.invc_type=0 then '1' when i.invc_type=2 then '-1' end)||';'||"
                + " TO_CHAR(sum(case when i.invc_type=0 then (ii.qty*ii.price)"
                + " when i.invc_type=2 then (ii.qty*ii.price)*-1 end ),'FM999G999G999G990D00','nls_numeric_characters=,.')||';'||"
                + " sum(case when i.invc_type=0 then ii.qty"
                + " when i.invc_type=2 then ii.qty*-1 end )||';'||"
                + " min(case when i.invc_type=0 then 'Regular' when i.invc_type=2 then 'Return' end) movimiento"
                + " from invoice i"
                + " inner join invc_item ii on i.invc_sid=ii.invc_sid"
                + " inner join invn_sbs t6 on ii.item_sid = t6.item_sid and t6.sbs_no=i.sbs_no"
                + " inner join employee t7 on ii.clerk_id = t7.empl_id and t7.sbs_no=i.sbs_no"
                + " inner join store s on (s.store_no=i.store_no and s.sbs_no=i.sbs_no)"
                + " left join customer c on (c.cust_sid=i.cust_sid)"
                + " left join cust_address ca on (ca.cust_sid=c.cust_sid and ca.addr_no=1 and c.cust_type=0)"
                + " left join country cc on (cc.country_id=ca.country_id)"
                + " inner join subsidiary sb on (sb.sbs_no=i.sbs_no)"
                + " where i.sbs_no=" + sbsNo + ""
                + " and i.store_no=" + store_no + ""
                + " and i.invc_no>0"
                + " and  i.created_date between to_date('01/01/2022 00:00:00', 'DD/MM/YYYY HH24:MI:SS')"
                + " and  to_date('31/01/2022 23:59:59', 'DD/MM/YYYY HH24:MI:SS')"
                + " group by i.invc_sid, i.created_date, s.store_name,  c.first_name, c.last_name, c.cust_id,"
                + " sb.sbs_no, s.SBS_NO||''||s.STORE_NO||''||s.STORE_CODE, cc.country_name, t7.RPRO_FULL_NAME,"
                + " t6.description1,i.store_no"
                + " order by i.created_date";
        final List<Map<String, Object>> sellsList = jdbcTemplate.queryForList(sells);
        String bodyFtpFile = "";
        Integer i = 0;
        for (final Map sell : sellsList) {
            String cadenaTexto = (String) sell.get("movimiento");
            bodyFtpFile += cadenaTexto.trim() + "\r\n";
            i++;
        }
        System.out.println("file sells" + sbsNo + "-" + store_no);
        saveFtpFile.CreateFile(bodyFtpFile, "sells");
        return "total " + i;

    }

}
