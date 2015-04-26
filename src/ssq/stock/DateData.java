package ssq.stock;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import ssq.utils.MathUtils;

public class DateData
{
    long  date;
    int   val;  // 收盘价
    float scale;

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
    
    public DateData(long date, int s)
    {
        this(date, s, 1);
    }

    public DateData(long date, int s, float scale)
    {
        this.scale = scale;
        this.date = date;
        val = s;
    }

    public int getScaledVal()
    {
        return MathUtils.round(val * scale);
    }

    public static DateData getNext(InputStream in, float scale) throws IOException
    {
        long date = MathUtils.getNextInt(in);
        in.skip(12);
        DateData result = new DateData(date, MathUtils.getNextInt(in), scale);
        in.skip(12);
        return result;
    }
}
