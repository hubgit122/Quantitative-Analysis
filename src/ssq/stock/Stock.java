package ssq.stock;

import java.io.File;
import java.io.IOException;

public class Stock
{
    public boolean isShangHai;
    public int     number;
    public StockHistory history;
    
    /**
     *
     * @param sh
     * @param num
     * @param firstDay
     *            参与计算的最早的一个交易日距今的交易日数
     * @param lastDay
     *            参与计算的最今的一个交易日距今的交易日数
     * @throws IOException
     */
    public Stock(File file, int firstDay, int lastDay) throws IOException
    {
        String name = file.getName();
        isShangHai = name.startsWith("sh");
        number = Integer.valueOf(name.substring(2, 8), 10);

        history = new StockHistory(this, file, firstDay, lastDay);
    }
    
    @Override
    public String toString()
    {
        return (isShangHai ? "sh" : "sz") + number;
    }
    
    public static String pad(int i)
    {
        char[] result = new char[6];

        for (int j = 5; j >= 0; j--)
        {
            result[j] = (char) ('0' + i % 10);
            i /= 10;
        }

        return new String(result);
    }
}
