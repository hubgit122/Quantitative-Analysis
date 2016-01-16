package ssq.utils.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ssq.utils.HashGenerator;

public class MD5Utils extends HashGenerator
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

    public MD5Utils()
    {
        super(16, -1);
    }
    
    @Override
    public byte[] hash(byte[] m)
    {
        synchronized (mdInst)
        {
            mdInst.update(m);
            return mdInst.digest();
        }
    }
}