package ssq.stock;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import ssq.utils.MathUtils;

public class DateData
{
    public int   date;
    public int   opening;  // 开盘价
    public int   highest; //高
    public int   lowest; //低
    public int   closing;    // 收盘价
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

    public DateData(int date, int k, int g, int d, int s, float scale)
    {
        this.date = date;
        this.scale = scale;

        opening = k;
        highest = g;
        lowest = d;
        closing = s;
    }

    public int getScaledVal()
    {
        return MathUtils.round(closing * scale);
    }
    
    public static DateData getNext(DataInputStream in, float scale) throws IOException
    {
        int date = MathUtils.readLittleEndianInt(in);
        int k = MathUtils.readLittleEndianInt(in);
        int g = MathUtils.readLittleEndianInt(in);
        int d = MathUtils.readLittleEndianInt(in);
        int s = MathUtils.readLittleEndianInt(in);

        DateData result = new DateData(date, k, g, d, s, scale);
        in.skip(12);
        return result;
    }
}