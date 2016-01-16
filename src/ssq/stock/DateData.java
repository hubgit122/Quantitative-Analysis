package ssq.stock;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import ssq.stock.interpreter.ReflectTreeBuilder.ValueType;
import ssq.utils.StringUtils;

/**
 * 日线的数据点
 *
 * @author s
 */
public class DateData implements Serializable
{
    private static final long            serialVersionUID = -2889383443934619874L;
    public int                           date;
    public float[]                       vals             = new float[7];                    //开 高 收 低 量 额 复权
                                                                                              
    public static final SimpleDateFormat format           = new SimpleDateFormat("yyyyMMdd");

    /**
     * 将一个格式为yyyyMMdd的8位数字转化为Date
     *
     * @param num
     * @return
     */
    @SuppressWarnings("deprecation")
    public static Date numberToDate(int num)
    {
        return new Date(num / 10000 - 1900, num % 10000 / 100 - 1, num % 100);
    }

    /**
     * dateToNumber的逆
     *
     * @param date
     * @see ssq.stock.DateData.dateToNumber
     * @return
     */
    @SuppressWarnings("deprecation")
    public static int dateToNumber(Date date)
    {
        return ((date.getYear() + 1900) * 100 + date.getMonth() + 1) * 100 + date.getDate();
    }

    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(date).append('[').append(StringUtils.join(",", vals)).append(']');
        return sb.toString();
    }
    
    public DateData(int date, float k, float g, float s, float d, int sum, float qunt, float scale)
    {
        this.date = date;

        int i = 0;
        vals[i++] = k;
        vals[i++] = g;
        vals[i++] = s;
        vals[i++] = d;
        vals[i++] = sum;
        vals[i++] = qunt;
        vals[i++] = scale;
    }
    
    public DateData(int date, float[] vals)
    {
        this.date = date;
        this.vals = vals;
    }
    
    /**
     * 得到ValueType所指称的数据值
     *
     * @param type
     * @return
     */
    public float getVal(ValueType type)
    {
        return getVal(type.ordinal());
    }

    /**
     * 得到ValueType所指称的数据的后复权值
     * 
     * @param type
     * @return
     */
    public float getScaledVal(ValueType type)
    {
        return getScaledVal(type.ordinal());
    }
    
    public float getVal(int index)
    {
        if (index >= 4)
        {
            return vals[index];
        }
        else
        {
            return vals[index] / vals[6];
        }
    }

    public float getScaledVal(int index)
    {
        return vals[index];
    }
}