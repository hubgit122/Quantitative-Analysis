package ssq.utils;

public class SqlServerAccesser extends SqlAccesser
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
    
    private String username, pass;
    
    public SqlServerAccesser(String dbname)
    {
        this(dbname, "localhost", "1433", null, null);
    }

    public SqlServerAccesser(String dbname, String url, String port, String username, String pass)
    {
        super(dbname, "jdbc:sqlserver://" + url + ":" + port + ";databaseName=" + dbname);
        this.username = username;
        this.pass = pass;
    }

    @Override
    void tryCreate()
    {
    }

    @Override
    boolean dbExists()
    {
        return true;
    }
    
    @Override
    public void updateDatabase()
    {
    }
}
