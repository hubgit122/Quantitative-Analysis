package ssq.stock;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import ssq.utils.DirUtils;

/**
 * 存放日线数据, 可以计算日线数据的函数<br>
 * 最近一天的数据是最后一个元素
 *
 * @author s
 */
public class History extends Vector<DateData>
{
    private static final long serialVersionUID = 1L;

    Stock                     stock;

    String pad(int i)
    {
        char[] result = new char[10];
        
        for (int j = 5; j >= 0; j--)
        {
            result[j] = (char) ('0' + i % 10);
            i /= 10;
        }

        result[6] = '.';
        result[7] = 'd';
        result[8] = 'a';
        result[9] = 'y';
        
        return new String(result);
    }

    public History(Stock s) throws FileNotFoundException
    {
        stock = s;
        //FileInputStream fin = new FileInputStream(new File(DirUtils.getProjectRoot() + "../光大证券/vipdoc/" + (stock.isShangHai ? "sh/lday/sh" : "sz/lday/sz") + stock.number));
        FileInputStream fin = new FileInputStream(new File("X:/光大证券/vipdoc/" + (stock.isShangHai ? "sh/lday/sh" : "sz/lday/sz") + pad(stock.number)));

        for (;;)
        {
            try
            {
                add(DateData.getNext(fin));
            }
            catch (IOException e)
            {
                return;
            }
        }
    }

    int func(String method, int firstDay, int lastDay)
    {
        switch (method)
        {
            case "min":
                return min(firstDay, lastDay);
                
            case "max":
                return max(firstDay, lastDay);
                
            default:
                return -1;
        }
    }

    private int max(int firstDay, int lastDay)
    {
        int result = Integer.MIN_VALUE;
        int size = size();
        
        for (int i = size - firstDay; i <= size - lastDay; i++)
        {
            int s = get(i).S;

            if (s > result)
            {
                result = s;
            }
        }
        return result;
    }

    private int min(int firstDay, int lastDay)
    {
        int result = Integer.MAX_VALUE;
        int size = size();
        
        for (int i = size - firstDay; i <= size - lastDay; i++)
        {
            int s = get(i).S;

            if (s < result)
            {
                result = s;
            }
        }
        return result;
    }
}
