package ssq.utils;

/**
 * 这是sqlserver的适配器类, 不能直接使用它. 使用sqlite的时候是要操作某些table的, 所以要继承这个类, 调用checkDatabase函数来初始化db和table
 *
 * @see ssq.utils.SqlAccesser.checkDatabase
 * @author s
 */
public abstract class SqlServerAccesser extends SqlAccesser
{
    static
    {
        try
        {
            Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public SqlServerAccesser(String dbname)
    {
        this(dbname, "localhost", "1433", null, null);
    }
    
    public SqlServerAccesser(String dbname, String url, String port, String username, String pass)
    {
        super(dbname, "jdbc:sqlserver://" + url + ":" + port + ";databaseName=" + dbname, "sa", "00");
    }
}
