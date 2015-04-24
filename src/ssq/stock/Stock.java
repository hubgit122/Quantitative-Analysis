package ssq.stock;

import java.io.FileNotFoundException;

public class Stock
{
    boolean isShangHai;
    int     number;
    History history;

    public Stock(boolean sh, int num) throws FileNotFoundException
    {
        isShangHai = sh;
        number = num;
        history = new History(this);
    }
}
