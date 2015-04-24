package ssq.utils;

import java.util.HashMap;
import java.util.Map;

public class Utilities
{
                                                                                          
    public static final Boolean        LOG_ON          = true;
    public static final Boolean        DEBUG_ON        = true;                           //预处理也有debug版的选项
                                                                                          
    public static Map<String, SqliteAccesser> sqliteAccessers = new HashMap<String, SqliteAccesser>();
    
    public static SqliteAccesser addSqliteAccesser(String sqliteAccesserName)
    {
        if (sqliteAccessers.containsKey(sqliteAccesserName))
        {
            return sqliteAccessers.get(sqliteAccesserName);
        }
        else
        {
            SqliteAccesser sqliteAccesser = new SqliteAccesser(sqliteAccesserName);
            sqliteAccessers.put(sqliteAccesserName, sqliteAccesser);
            return sqliteAccesser;
        }
    }
    
}
