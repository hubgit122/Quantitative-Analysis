package ssq.utils;

import it.sauronsoftware.base64.Base64;

/** */
/**
 * <p>
 * BASE64编码解码工具包
 * </p>
 * <p>
 * 依赖javabase64-1.3.1.jar
 * </p>
 * 
 * @author IceWee
 * @date 2012-5-19
 * @version 1.0
 */
public class Base64Utils
{
    /** */
    /**
     * <p>
     * BASE64字符串解码为二进制数据
     * </p>
     * 
     * @param base64
     * @return
     * @throws Exception
     */
    public static byte[] decode(String base64)
    {
        return decode(base64.getBytes());
    }
    
    public static byte[] decode(byte[] base64)
    {
        try
        {
            return Base64.decode(base64);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    /** */
    /**
     * <p>
     * 二进制数据编码为BASE64字符串
     * </p>
     * 
     * @param bytes
     * @return
     * @throws Exception
     */
    public static String encode(byte[] bytes) throws Exception
    {
        return new String(Base64.encode(bytes));
    }
    
    public static String encode(String str) throws Exception
    {
        return new String(Base64.encode(str.getBytes()));
    }
}
