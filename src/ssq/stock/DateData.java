package ssq.stock;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class DateData
{
    int G, D, K, S; // 高, 低, 开, 收

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
    
    private static int getNextInt(InputStream in) throws IOException
    {
        int ch = 0;
        int d = 0;

        for (int i = 0; i < 32; i += 8)
        {
            ch = in.read();
            if (ch < 0)
                throw new EOFException();

            d += ch << i;
        }
        return d;
    }

    public DateData(int k, int g, int d, int s)
    {
        K = k;
        G = g;
        D = d;
        S = s;
    }

    public static DateData getNext(InputStream in) throws IOException
    {
        in.skip(4);
        DateData result = new DateData(getNextInt(in), getNextInt(in), getNextInt(in), getNextInt(in));
        in.skip(12);
        return result;
    }
}
