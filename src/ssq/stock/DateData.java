package ssq.stock;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.Date;

import ssq.utils.MathUtils;

public class DateData
{
    public int   date;
    public int   start; // 开盘价
    public int   end;  // 收盘价
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
    
    public DateData(int date, int k, int s, float scale)
    {
        this.date = date;
        this.scale = scale;
        start = k;
        end = s;
    }
    
    public int getScaledVal()
    {
        return MathUtils.round(end * scale);
    }

    public static DateData getNext(DataInputStream in, float scale) throws IOException
    {
        int date = MathUtils.readLittleEndianInt(in);
        int k = MathUtils.readLittleEndianInt(in);
        in.skip(8);
        DateData result = new DateData(date, k, MathUtils.readLittleEndianInt(in), scale);
        in.skip(12);
        return result;
    }
}