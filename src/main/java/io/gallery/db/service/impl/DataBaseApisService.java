package io.gallery.db.service.impl;

import io.gallery.db.service.IDataBaseApis;
import io.gallery.db.util.DBT;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class DataBaseApisService extends DataBaseGenericService implements IDataBaseApis {
    private final Log logger = LogFactory.getLog(getClass());

    DataBaseApisService() {
        tableName = "apis";
    }

    @Override
    public void auth(String table) {
        String prefix = null;
        String tableName = null;
        if (DBT.isNotNull(table)) {
            if (table.contains(".")) {
                String[] split = table.split("\\.");
                prefix = split[0];
                tableName = split[1];
            } else {
                tableName = table;
            }
            HashMap map = new HashMap();
            map.put("prefix", prefix);
            map.put("tablename", tableName);
            try {
                HashMap record = (HashMap) selectOne(map);
                if (record != null) {

                } else {
                    insert(map);
                }
            } catch (Exception e) {
                logger.error("auth error: " + e.getMessage());
            }
        }
    }

}
