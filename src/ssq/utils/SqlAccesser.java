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
    private String     url, dbname;
    
    public SqlAccesser(String dbname, String url)
    {
        this.url = url;
        this.dbname = dbname;
        
        try
        {
            connection = DriverManager.getConnection(getJDBCPath(), "sa", "00");
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
     * 如果指定DB不存在, 则用指定的sql语句创建DB和Table
     */
    public void checkDatabase(String sql, String version)
    {

        if (version == null)
        {
            version = "1.0";
        }
        
        if (dbExists())
        {
            updateDatabase();
        }

        tryCreate();

        try
        {
            update("create table version(version varchar(100))", null);
            update("insert into version values(?)", new Object[] { version });
        }
        catch (Exception e)
        {
        }

        try
        {
            update(sql, null);
        }
        catch (Exception e)
        {
        }
    }
    
    abstract void tryCreate();
    
    abstract boolean dbExists();
    
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
    public List<Map<String, Object>> query(String sql, Object[] args, String[] keys) throws Exception
    {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (null == keys || keys.length == 0)
        {
            return list;
        }
        
        Object[] result = queryWithStatement(sql, args);
        Statement statement = (Statement) result[0];
        ResultSet resultSet = (ResultSet) result[1];
        while (resultSet.next())
        {
            Map<String, Object> map = new HashMap<String, Object>();
            for (int i = 0; i < keys.length; i++)
            {
                map.put(keys[i], resultSet.getString(keys[i]));
            }
            list.add(map);
        }
        close(statement, resultSet);
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
     * 执行sql返回statement和resultSet
     *
     * @param sql
     *            sql语句
     * @param args
     *            参数列表
     * @return Map
     * @throws Exception
     */
    public Object[] queryWithStatement(String sql, Object[] args) throws Exception
    {
        Object[] result = new Object[2];
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
        
        result[0] = statement;
        result[1] = resultSet;

        return result;
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
            close(prepared, null);
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
    
    /**
     * 关闭连接
     *
     * @param statement
     *            预编译语句
     * @param resultSet
     *            结果集
     * @throws Exception
     */
    public void close(Statement statement, ResultSet resultSet) throws Exception
    {
        if (null != resultSet)
        {
            resultSet.close();
            resultSet = null;
        }
        if (null != statement)
        {
            statement.close();
            statement = null;
        }
    }
}
