package ssq.utils;


public class LogUtils
{
    public static final String LOG_TAG     = "ssq";
    public static final String WARNING_TAG = "ssq warning";
    
    public static void logString(String message, String tag, boolean onlyDebug)  // TO-DO 改名字
    {
        if (onlyDebug && !Utilities.DEBUG_ON && !true || !Utilities.LOG_ON)
        {
            return;
        }
        System.err.println((tag == null ? LOG_TAG : tag) + " : " + message); //#ifdef Java
    }
    
    public static void logString(String message)
    {
        logString(message, null, false);
    }
    
    public static void logWarningString(String message)
    {
        logWarningString(message, null, false);
    }
    
    public static void logWarningString(String message, String tag, boolean onlyDebug)
    {
        if (onlyDebug && !Utilities.DEBUG_ON && !true || !Utilities.LOG_ON)
        {
            return;
        }
        System.err.println((tag == null ? WARNING_TAG : tag) + " : " + message); //#ifdef Java
    }
}
