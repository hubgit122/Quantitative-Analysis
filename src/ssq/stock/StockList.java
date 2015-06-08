package ssq.stock;

import java.util.ArrayList;

import ssq.utils.Pair;

public class StockList extends ArrayList<Pair<Integer, String>>
{
    private static final long serialVersionUID = -4394026989859742527L;
    
    public StockList()
    {
        super(5000);
    }

    public StockList(StockList stockList)
    {
        super(stockList);
    }
    
    /**
     *
     * @param ind
     * @return 如果已经存在这个元素, 返回现在indece加1的相反数, 否则返回应该插入的位置
     */
    public int findInsertIndex(int ind)
    {
        int start = 0;
        int end = size() - 1;
        
        if (end == -1)
        {
            return 0;
        }
        else
        {
            while (end >= start)
            {
                int mid = (start + end) / 2;
                int midNum = get(mid).getKey();

                if (ind < midNum)
                {
                    end = mid - 1;
                }
                else if (ind > midNum)
                {
                    start = mid + 1;
                }
                else
                {
                    return -(mid + 1);
                }
            }

            return start;
        }
    }
    
    @Override
    public boolean add(Pair<Integer, String> e)
    {
        int index = findInsertIndex(e.getKey());

        if (index >= 0)
        {
            super.add(index, e);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    @Override
    public boolean contains(Object o)
    {
        return (o instanceof Stock && findInsertIndex(((Stock) o).number) < 0);
    }

    public String getNameOfId(int id)
    {
        int index = findInsertIndex(id);

        if (index < 0)
        {
            return get(-index - 1).getValue();
        }
        else
        {
            return null;
        }
    }
}
