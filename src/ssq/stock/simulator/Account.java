package ssq.stock.simulator;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;
import java.util.LinkedList;

import ssq.stock.Stock;
import ssq.stock.simulator.Deal.Type;

/**
 * 一个账户. 有买卖委托列表, 持仓列表, 交易记录
 *
 * @author s
 *
 */
public class Account implements Serializable
{
    private static final long serialVersionUID = 1L;
    
    /**
     * 账户名称
     */
    String                    name;
    /**
     * 可用资金 不模拟卖出股票得到的资金第二天才可以取, 比较复杂
     */
    float                     available;
    /**
     * 冻结资金, 已经被委托了
     */
    float                     freezed          = 0;

    transient Simulator       simulator;

    LinkedList<Commission>    commissionList   = new LinkedList<>();
    LinkedList<Holded>        holdedList       = new LinkedList<>();

    Deals                     deals            = new Deals();
    
    public Account(String name, float iniVal)
    {
        this.name = name;
        available = iniVal;
    }
    
    /**
     * 得到总资产
     *
     * @return 总钱数+所有股票当前市值
     * @throws IOException
     */
    public float getTotalAssets() throws IOException
    {
        float result = available + freezed;
        for (Holded holded : holdedList)
        {
            result += holded.getCurrentMarketCapitalization();
        }
        
        return result;
    }
    
    /**
     * 得到某股票的持仓
     */
    public Holded getHolded(Stock stock)
    {
        for (Holded holded : holdedList)
        {
            if (holded.stock.getCode() == stock.getCode())
            {
                return holded;
            }
        }
        
        return new Holded(stock, 0, 0);
    }
    
    /**
     * 给账户存款
     *
     * @return 成功: 交易编号, 失败: -1
     */
    public int deposit(Date time, float val)
    {
        if (val > 0)
        {
            available += val;
            return deals.add(time, val, 1, Type.DEPOSIT, simulator);
        }
        return -1;
    }
    
    /**
     * 从账户取款
     *
     * @return 成功: 交易编号, 失败: -1
     */
    public int withdraw(Date time, float val)
    {
        if (val > 0 && val <= available)
        {
            available -= val;
            
            return deals.add(time, val, 1, Type.WITHDRAW, simulator);
        }
        
        return -1;
    }
    
    /**
     * 买入股票
     *
     * @return 成功: 交易编号, 失败: -1
     */
    public int buy(Stock stock, float price, int quantity, Date time)
    {
        Holded holded = getHolded(stock);
        if (available >= price * quantity && holded.buy(price, quantity))
        {
            if (holded.getTotalQuantity() == quantity)
            {
                holdedList.add(holded);
            }
            
            return deals.add(time, price, quantity, Type.BUY, simulator);
        }
        return -1;
    }
    
    /**
     * 委托买入
     *
     * @return 委托是否成功提交
     */
    public boolean commitBuy(Stock stock, float price, int quantity, Date time)
    {
        if (available >= price * quantity && getHolded(stock).commitBuy(price, quantity))
        {
            commissionList.add(new Commission(stock, price, quantity, true, time));
            return true;
        }
        
        return false;
    }
    
    /**
     * 卖出股票
     *
     * @return 是否成功
     */
    public int sell(Stock stock, float price, int quantity, Date time)
    {
        if (getHolded(stock).sell(price, quantity))
        {
            float total = price * quantity;
            available += total - Simulator.getCommissionFee(total) - Simulator.getStampTax(total);
            
            return deals.add(time, price, quantity, Type.SELL, simulator);
        }
        
        return -1;
    }
    
    /**
     * 查询是否可以卖出
     *
     * @return 有足够的可用持仓
     */
    public boolean canSell(Stock stock, int quantity)
    {
        return getHolded(stock).canSell(quantity);
    }
    
    /**
     * 委托卖出
     *
     * @return 委托是否成功提交
     */
    public boolean commitSell(Stock stock, float price, int quantity, Date time)
    {
        if (getHolded(stock).commitSell(price, quantity))
        {
            commissionList.add(new Commission(stock, price, quantity, false, time));
            return true;
        }
        return false;
    }

    /**
     * 委托成交了, 将委托转化为成交记录, 并改动账户相关信息
     */
    public void commissionMet(Commission commission, Date time)
    {
        commissionList.remove(commission);
        
        if (commission.buy)
        {
            buy(commission.stock, commission.price, commission.quantity, time);
        }
        else
        {
            sell(commission.stock, commission.price, commission.quantity, time);
        }
    }
}
