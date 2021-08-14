package sainthonore.api.sendFtp.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import sainthonore.api.sendFtp.model.SellModel;

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

    public List<SellModel> getSells() throws NoSuchAlgorithmException, NoSuchProviderException, ParseException {

        final String sql = "";

        final List<SellModel> eventosliquidados = new ArrayList<>();

        final List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

        for (final Map row : rows) {
            final SellModel obj = new SellModel();

            eventosliquidados.add(obj);
        }
        return eventosliquidados;
    }

}
