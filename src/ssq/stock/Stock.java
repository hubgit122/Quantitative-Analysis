package ssq.stock;

import java.io.FileNotFoundException;
import java.io.IOException;

public class Stock
{
    boolean isShangHai;
    int     number;
    History history;
    
    public Stock(boolean sh, int num) throws IOException
    {
        this(sh, num, -1, -1);
    }
    
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
    public Stock(boolean sh, int num, int firstDay, int lastDay) throws IOException
    {
        isShangHai = sh;
        number = num;
        history = new History(this, firstDay, lastDay);

    }
}
