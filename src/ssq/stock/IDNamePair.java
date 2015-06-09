package ssq.stock;

import ssq.utils.Pair;

public class IDNamePair extends Pair<Integer, String>
{
    public IDNamePair(Integer k, String v)
    {
        super(k, v);
    }

    @Override
    public String toString()
    {
        return Stock.pad(getKey()) + getValue();
    }
}
