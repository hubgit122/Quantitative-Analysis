package ssq.stock.simulator;

import java.io.IOException;
import java.io.Serializable;

import ssq.stock.Stock;

/**
 * 一条持仓记录
 *
 * @author s
 */
public class Holded implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /**
     * 持有的股票
     */
    Stock                     stock;

    /**
     * 即时可卖出的股票数, 不是手数
     */
    int                       available        = 0;

    /**
     * 因委托而冻结的股票数, 不是手数
     */
    int                       committed        = 0;

    /**
     * 因今天刚买入而冻结的股票数, 不是手数
     */
    int                       T0               = 0;

    /**
     * 每股的成本
     */
    float                     cost             = 0;

    /**
     * 得到当前本持仓的市值
     *
     * @throws IOException
     */
    public float getCurrentMarketCapitalization() throws IOException
    {
        return stock.getCurrentPrice() * getQuantity();
    }
    
    /**
     * @return 总股票数, 不是手数
     */
    private int getQuantity()
    {
        return available + T0 + committed;
    }

    /**
     * 计算如果现在卖出, 可以得到多少钱<br>
     * 注: 由于我们实际的钱不多, 即使模拟的时候金额巨大, 也不考虑买2的价格, 只以当前买1的价格成交
     *
     * @throws IOException
     */
    public float getCurrentAccessableCapitalization() throws IOException
    {
        return stock.getCurrentMarketBuy() * getQuantity();
    }
    
    /**
     * 得到本持仓的收益
     *
     * @throws IOException
     */
    public float getIncome() throws IOException
    {
        return (stock.getCurrentPrice() - cost) * getQuantity();
    }

    /**
     * @return 持有总数
     */
    public int getTotalQuantity()
    {
        return available + committed + T0;
    }
    
    /**
     * 构造
     *
     * @param s
     * @param price
     * @param quantity
     */
    public Holded(Stock s, float price, int quantity)
    {
        stock = s;
        buy(price, quantity);
    }

    /**
     * 追加买入, 只能由模拟器调用
     */
    public boolean buy(float price, int addedQuantity)
    {
        if (!canBuy(addedQuantity))
        {
            System.err.println("瞎买");
            return false;
        }

        int quantityAfter = getQuantity() + addedQuantity;
        cost = (cost * getQuantity() + price * addedQuantity + Simulator.getCommissionFee(price * addedQuantity)) / quantityAfter;
        
        T0 += addedQuantity;
        return true;
    }

    public boolean canBuy(int addedQuantity)
    {
        return (addedQuantity > 0 && addedQuantity % 100 == 0);
    }

    /**
     * 委托卖出
     *
     * @return 是否成功
     */
    public boolean commitBuy(float price, int quantity)
    {
        return canBuy(quantity);
    }

    /**
     * 部分卖出, 只能由模拟器调用
     */
    public boolean sell(float price, int subedQuantity)
    {
        if (!canSell(subedQuantity))
        {
            System.err.println("瞎卖");
            return false;
        }

        int quantityAfter = getQuantity() - subedQuantity;
        cost = (cost * getQuantity() - price * subedQuantity + Simulator.getCommissionFee(price * subedQuantity) + Simulator.getStampTax(price * subedQuantity)) / quantityAfter;
        
        available -= subedQuantity;
        return true;
    }
    
    public boolean canSell(int subedQuantity)
    {
        return (subedQuantity > 0 && subedQuantity % 100 == 0 && subedQuantity <= available);
    }

    /**
     * 委托卖出
     *
     * @return 是否成功
     */
    public boolean commitSell(float price, int quantity)
    {
        if (canSell(quantity))
        {
            committed += quantity;
            available -= quantity;
            return true;
        }
        
        return false;
    }
    
}
