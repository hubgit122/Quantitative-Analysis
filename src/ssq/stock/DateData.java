package ssq.stock;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import ssq.stock.interpreter.ReflectTreeBuilder.ValueType;
import ssq.utils.MathUtils;

public class DateData
{
    public int   date;
    public int[] vals = new int[6]; //开 高 低 收 量 上次收盘
    public float amountOfDeal;     //成交额
    public float scale;

    @SuppressWarnings("deprecation")
    public static Date numberToDate(int num)
    {
        return new Date(num / 10000 - 1900, num % 10000 / 100 - 1, num % 100);
    }
    
    @SuppressWarnings("deprecation")
    public static int dateToNumber(Date date)
    {
        return (date.getYear() * 100 + date.getMonth() + 1) * 100 + date.getDate();
    }
    
    public DateData setScale(float scale)
    {
        this.scale = scale;
        return this;
    }

    public DateData(int date, int k, int g, int d, int s, float qunt, int sum, float scale)
    {
        this.date = date;
        this.scale = scale;
        
        int i = 0;
        vals[i++] = k;
        vals[i++] = g;
        vals[i++] = d;
        vals[i++] = s;
        amountOfDeal = qunt;
        vals[i++] = sum;
    }

    public int getScaledVal(ValueType type)
    {
        int index = type.ordinal();
        if (index == 4)
        {
            return vals[4];
        }
        else
        {
            return MathUtils.round(vals[index] * scale);
        }
    }
    
    public static DateData getNext(DataInputStream in, float scale) throws IOException
    {
        int date = MathUtils.readLittleEndianInt(in);
        int k = MathUtils.readLittleEndianInt(in);
        int g = MathUtils.readLittleEndianInt(in);
        int d = MathUtils.readLittleEndianInt(in);
        int s = MathUtils.readLittleEndianInt(in);
        float a = MathUtils.readLittleEndianFloat(in);
        int sum = MathUtils.readLittleEndianInt(in);
        int last = MathUtils.readLittleEndianInt(in);
        
        DateData result = new DateData(date, k, g, d, s, a, sum, scale);
        
        return result;
    }
}