package ssq.stock.simulator;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import ssq.utils.StringUtils;

/**
 * 一笔成交记录<br>
 *
 * @author s
 */
public class Deal implements Serializable
{
    SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public enum Type
    {
        BUY, SELL, DEPOSIT, WITHDRAW, OTHERS
    }
    
    private static final long serialVersionUID = 1L;
    
    final private Date        time;
    final private float       price;
    final private int         quantity;
    final private Type        type;
    final private int         num;

    public Deal(Date time, float price, int quantity, Type type, int num)
    {
        this.time = time;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
        this.num = num;
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder(timeFormat.format(getTime()));
        sb.append(' ').append(getPrice()).append(' ').append(quantity).append(' ').append(type.toString()).append(' ').append(StringUtils.pad(String.valueOf(num), 8, '0', false));
        return sb.toString();
    }

    public int getNum()
    {
        return num;
    }

    public Date getTime()
    {
        return time;
    }

    public float getPrice()
    {
        return price;
    }

    public int getQuantity()
    {
        return quantity;
    }
    
    public Type getType()
    {
        return type;
    }
}
