package ssq.stock.simulator;

import java.io.Serializable;
import java.util.Date;

import ssq.stock.Stock;

/**
 * 一条委托交易记录
 *
 * @author s
 */
public class Commission implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     * 申报的股票
     */
    Stock                     stock;
    /**
     * 申报的股数, 不是手数
     */
    int                       quantity;
    /**
     * 申报的价格
     */
    float                     price;
    /**
     * true: 申报买, false: 申报卖
     */
    boolean                   buy;
    /**
     * 委托时间
     */
    Date                      time;

    public Commission(Stock s, float price, int quant, boolean isBuy, Date time)
    {
        stock = s;
        quantity = quant;
        this.price = price;
        this.buy = isBuy;
        this.time = time;
    }
}
