package ssq.utils;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SqlAccesser
{
    private Connection connection;
    protected String   url, dbname;
    
    /**
     * 子类必须显式地指出当前数据库的版本.
     *
     * @return 版本号
     */
    abstract public String getVersion();
    
    public SqlAccesser(String dbname, String url)
    {
        this(dbname, url, null, null);
    }
    
    public SqlAccesser(String dbname, String url, String usr, String pass)
    {
        this.url = url;
        this.dbname = dbname;

        try
        {
            connection = usr != null && pass != null ? DriverManager.getConnection(getJDBCPath(), usr, pass) : DriverManager.getConnection(getJDBCPath());
        }
        catch (SQLException e)
        {
            e.printStackTrace();
        }
    }

    public String getJDBCPath()
    {
        return url;
    }

    /**
     * 若指定DB存在, 按版本号更新DB. 否则, 尝试创建DB, 并写入版本信息. <br>
     * 尝试创建必须使用的Table, 后面对DB的操作可以认为这些Table存在.<br>
     * 这个函数不能在本类的构造函数里调用, 可以在子类初始化version字段之后调用.
     */
    public void checkDatabase()
    {
        if (dbExists())
        {
            if (updateVersion().compareTo(getVersion()) < 0)
            {
                updateDatabase();
            }
        }
        
        tryCreateDB();

        insertVersion(getVersion());
        
        try
        {
            tryCreateTable();
        }
        catch (Exception e)
        {
        }
    }

    /**
     * 更新版本号
     *
     * @return 旧的版本号, 0.0 代表没有版本号
     */
    protected String updateVersion()
    {
        String oldVersion = getLastVersion();
        String newVersion = getVersion();
        
        if (newVersion.compareTo(oldVersion) > 0)
        {
            try
            {
                update("drop table version", null);
            }
            catch (Exception e)
            {
            }

            insertVersion(newVersion);
        }

        return oldVersion;
    }

    public String getLastVersion()
    {
        String oldVersion = "0.0";
        ResultSet results = null;
        try
        {
            results = query("select version from version", null);
            
            if (results.next())
            {
                String ver = results.getString("version");
                if (ver != null)
                {
                    oldVersion = ver;
                }
            }
        }
        catch (Exception e)
        {
            System.out.println(e.getLocalizedMessage());
        }
        finally
        {
            try
            {
                results.getStatement().close();
            }
            catch (SQLException e)
            {
            }
        }

        return oldVersion;
    }
    
    public void insertVersion(String newVersion)
    {
        try
        {
            update("create table version(version varchar(100))", null);
        }
        catch (Exception e)
        {
        }

        try
        {
            update("insert into version values(?)", new Object[] { newVersion });
        }
        catch (Exception e)
        {
        }
    }

    /**
     * 尝试创建必须使用的Table, 后面对DB的操作可以认为这些Table存在.
     */
    abstract protected void tryCreateTable();

    /**
     * 尝试创建DB
     */
    abstract protected void tryCreateDB();

    abstract protected boolean dbExists();

    /**
     * 对数据库有更新要求时, 应该继承此类, 覆盖此方法. 在数据库里判断version域
     *
     * @param path
     */
    abstract public void updateDatabase();

    /**
     * 获得结果map的列表
     *
     * @param sql
     *            sql语句
     * @param args
     *            参数列表
     * @param keys
     *            取值的key
     * @return List
     * @throws Exception
     */
    public List<Map<String, String>> query(String sql, Object[] args, String[] keys) throws Exception
    {
        List<Map<String, String>> list = new ArrayList<Map<String, String>>();
        if (null == keys || keys.length == 0)
        {
            return list;
        }

        ResultSet resultSet = query(sql, args);
        while (resultSet.next())
        {
            Map<String, String> map = new HashMap<String, String>();
            for (int i = 0; i < keys.length; i++)
            {
                map.put(keys[i], resultSet.getString(keys[i]));
            }
            list.add(map);
        }
        resultSet.getStatement().close();
        return list;
    }

    /**
     * 执行sql返回statement和resultSet
     *
     * @param connection
     *            连接
     * @param sql
     *            sql语句
     * @param args
     *            参数列表
     * @return Map
     * @throws Exception
     */
    public ResultSet query(String sql, Object[] args) throws Exception
    {
        Statement statement = null;
        ResultSet resultSet = null;
        if (null != args && args.length > 0)
        {
            statement = getPrepared(sql, args);
            resultSet = ((PreparedStatement) statement).executeQuery();
        }
        else
        {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(sql);
        }
        
        return resultSet;
    }

    /**
     * 操作数据库(增删改)
     *
     * @param sqls
     *            sql语句序列, 用;隔开
     * @param args
     *            参数列表
     * @return int 受影响的数据行数
     * @throws Exception
     */
    public int update(String sqls, Object[] args) throws Exception
    {
        int result = 0;
        for (String sql : sqls.split(";"))
        {
            PreparedStatement prepared = getPrepared(sql, args);
            result += prepared.executeUpdate();
            prepared.close();
        }
        return result;
    }

    /**
     * 获得预编译语句
     *
     * @param connection
     *            数据库连接
     * @param sql
     *            sql语句
     * @param args
     *            参数列表
     * @return PreparedStatement
     * @throws Exception
     */
    private PreparedStatement getPrepared(String sql, Object[] args) throws Exception
    {
        PreparedStatement prepared = connection.prepareStatement(sql);
        if (null != args && args.length > 0)
        {
            for (int i = 0; i < args.length; i++)
            {
                if (args[i] instanceof Integer)
                {
                    prepared.setInt(i + 1, (Integer) args[i]);
                }
                else if (args[i] instanceof Float)
                {
                    prepared.setFloat(i + 1, (Float) args[i]);
                }
                else if (args[i] instanceof String)
                {
                    prepared.setString(i + 1, (String) args[i]);
                }
                else if (args[i] instanceof Long)
                {
                    prepared.setLong(i + 1, (Long) args[i]);
                }
                else if (args[i] instanceof Date)
                {
                    prepared.setDate(i + 1, (Date) args[i]);
                }
                else
                {
                    prepared.setString(i + 1, null == args[i] ? "" : args[i].toString());
                }
            }
        }
        return prepared;
    }

}
