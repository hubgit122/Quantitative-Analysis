package ssq.utils;

import java.io.File;
import java.io.IOException;

/**
 * 这是sqlite的适配器类, 不能直接使用它. 使用sqlite的时候是要操作某些table的, 所以要继承这个类, 调用checkDatabase函数来初始化db和table
 *
 * @see ssq.utils.SqlAccesser.checkDatabase
 * @author s
 */
public class SqliteAccesser extends SqlAccesser
{
    static
    {
        try
        {
            Class.forName("org.sqlite.JDBC"); //#ifdef Java
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    String DBPath;
    
    public String getDBPath()
    {
        return DBPath;
    }
    
    public SqliteAccesser(String dbName)
    {
        super(dbName, "jdbc:sqlite:" + //#ifdef Java
                DirUtils.getDataBaseRoot() + File.separator + dbName + ".db");
        
        DBPath = DirUtils.getDataBaseRoot() + File.separator + dbName + ".db";
    }
    
    @Override
    public boolean dbExists()
    {
        return new File(getDBPath()).exists();
    }
    
    @Override
    public void tryCreate()
    {
        try
        {
            new File(getDBPath()).createNewFile();
        }
        catch (IOException e)
        {
        }
    }
    
    @Override
    public void updateDatabase()
    {
    }
}
