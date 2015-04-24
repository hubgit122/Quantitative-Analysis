package ssq.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5Utils
{
    private static MessageDigest mdInst;
    
    static
    {
        try
        {
            mdInst = MessageDigest.getInstance("MD5");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
    }
    
    public static byte[] getMD5Bytes(byte[] btInput)
    {
        mdInst.update(btInput);
        return mdInst.digest();
    }
}