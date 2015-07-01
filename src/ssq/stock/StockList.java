package ssq.stock;

import java.util.ArrayList;

/**
 * 存放股票代码到名称的映射的有序列表, 小代码在前<br>
 * IDNamePair的列表
 *
 * @see IDNamePair
 */
public class StockList extends ArrayList<IDNamePair>
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
     * 计算在列表中加入一个代码为ind的股票时, 它应该插入的位置
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
    
    /**
     * 在本列表中增加一个 IDNamePair
     */
    @Override
    public boolean add(IDNamePair e)
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

    /**
     * 判断列表中是否存在这个Stock对象对应的IDNamePair
     */
    @Override
    public boolean contains(Object o)
    {
        return (o instanceof Stock && findInsertIndex(((Stock) o).getNumber()) < 0);
    }

    /**
     * 从代码得到名称
     * 
     * @param id
     * @return
     */
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
