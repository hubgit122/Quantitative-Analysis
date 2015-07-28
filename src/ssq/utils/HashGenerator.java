package ssq.utils;

/**
 * 对象的哈希产生器, 提供hash(byte[])方法
 *
 * @author s
 */
public abstract class HashGenerator
{
    public int length;
    public int maxMessage;
    
    public HashGenerator(int l, int max)
    {
        length = l;
        maxMessage = max;
    }
    
    public abstract byte[] hash(byte[] m);
}