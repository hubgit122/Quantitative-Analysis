package ssq.stock;

import ssq.utils.Pair;

/**
 * 存放股票代码到名称的映射
 * 
 * @author s
 */
public class IDNamePair extends Pair<Integer, String>
{
    private static final long serialVersionUID = 1L;

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
