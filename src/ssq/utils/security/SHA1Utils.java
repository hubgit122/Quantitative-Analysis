package ssq.utils.security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import ssq.utils.HashGenerator;

public class SHA1Utils extends HashGenerator
{
    public SHA1Utils()
    {
        super(20, -1);
    }
    
    private static MessageDigest mdInst;

    static
    {
        try
        {
            mdInst = MessageDigest.getInstance("SHA1");
        }
        catch (NoSuchAlgorithmException e)
        {
            e.printStackTrace();
        }
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
