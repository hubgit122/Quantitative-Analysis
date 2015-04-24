package ssq.utils;

public class ArrayUtils
{
    public static byte[] xor(byte[] a, byte[] b)
    {
        byte[] result = new byte[a.length];
        for (int i = 0; i < a.length; i++)
        {
            result[i] = (byte) (a[i] ^ b[i]);
        }
        return result;
    }
    
    public static void xorNoCopy(byte[] dst, byte[] src, int offset, int len)
    {
        for (int i = offset; i < dst.length && i < offset + len; i++)
        {
            dst[i] ^= src[i - offset];
        }
    }
    
    public static void xorNoCopy(byte[] a, byte[] b)
    {
        xorNoCopy(a, b, 0, a.length);
    }
}
