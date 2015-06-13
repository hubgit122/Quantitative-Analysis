package ssq.utils;

public abstract class HashGenerator
{
    public int length;
    public int maxMessage;
    
    HashGenerator(int l, int max)
    {
        length = l;
        maxMessage = max;
    }
    
    public abstract byte[] hash(byte[] m);
}