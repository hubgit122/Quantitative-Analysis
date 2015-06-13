package ssq.utils;

import java.io.File;

public class DirUtils
{
    static
    {
        for (String name : new String[] { getProjectRoot(), getWebRoot(), getKeyRoot(), getDataBaseRoot(), getTmpRoot() })
        {
            ensureExistence(name);
        }
    }
    
    public static String ensureExistence(String name)
    {
        File tmp = new File(name);
        if (!tmp.exists())
        {
            tmp.mkdirs();
        }

        return name;
    }
    
    static String projectRoot         = null;
    static String writableProjectRoot = null;
    static String keyRoot             = null;
    static String webRoot             = null;
    static String dbRoot              = null;
    static String tmpRoot             = null;
    
    public static String getXxRoot(String name)
    {
        return getProjectRoot() + name + "/";
    }

    public static String getWritableXxRoot(String name)
    {
        return ensureExistence(getWritableProjectRoot() + name + "/");
    }
    
    public static String getProjectRoot()
    {
        if (projectRoot != null)
        {
            return projectRoot;
        }
        else
        {
            return (projectRoot = System.getProperty("user.dir") + "/");
        }
    }
    
    public static String getWritableProjectRoot()
    {
        if (writableProjectRoot != null)
        {
            return writableProjectRoot;
        }
        else
        {
            return (writableProjectRoot = System.getProperty("user.dir") + "/"); //#ifdef Java
        }
    }
    
    public static String getTmpRoot()
    {
        if (tmpRoot != null)
        {
            return tmpRoot;
        }
        else
        {
            return (tmpRoot = getWritableProjectRoot() + "tmp/");
        }
    }
    
    public static String getKeyRoot()
    {
        if (keyRoot != null)
        {
            return keyRoot;
        }
        else
        {
            return (keyRoot = getProjectRoot() + "assets/keys/");
        }
    }
    
    public static String getWebRoot()
    {
        if (webRoot != null)
        {
            return webRoot;
        }
        else
        {
            return (webRoot = getProjectRoot() + "assets/web/");
        }
    }
    
    public static String getDataBaseRoot()
    {
        if (dbRoot != null)
        {
            return dbRoot;
        }
        else
        {
            return (dbRoot = getWritableProjectRoot() + "db/");
        }
    }
}