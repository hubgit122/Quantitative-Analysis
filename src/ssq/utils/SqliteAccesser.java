package ssq.utils;

import java.io.File;
import java.io.IOException;

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
        super(dbName);

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
    public String getJDBCPath()
    {
        return "jdbc:sqlite:" + //#ifdef Java
                DBPath;
    }

    @Override
    public void updateDatabase()
    {
        // TODO 自动生成的方法存根
        
    }
    
}
