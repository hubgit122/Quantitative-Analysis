package ssq.utils;

import java.io.Serializable;

public final class Pair<K, V> implements Serializable
{
    
    private K k;
    private V v;

    public static <K, V> Pair<K, V> of(K k, V v)
    {
        return new Pair<K, V>(k, v);
    }
    
    public Pair(K k, V v)
    {
        this.k = k;
        this.v = v;
    }
    
    public K getKey()
    {
        return k;
    }
    
    public V getValue()
    {
        return v;
    }
    
    public void setKey(K k)
    {
        this.k = k;
    }

    public void setValue(V v)
    {
        this.v = v;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o == this)
            return true;
        if (!(o instanceof Pair))
            return false;
        Pair<?, ?> e = (Pair<?, ?>) o;
        return equal(k, e.getKey()) && equal(v, e.getValue());
    }
    
    public static boolean equal(Object a, Object b)
    {
        return a == b || (a != null && a.equals(b));
    }

    @Override
    public int hashCode()
    {
        return ((k == null) ? 0 : k.hashCode()) ^ ((v == null) ? 0 : v.hashCode());
    }
    
    @Override
    public String toString()
    {
        return k + "->" + v;
    }
    
}
