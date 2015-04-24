package ssq.stock;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
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
    
    public History(Stock s, int firstDay, int lastDay) throws IOException
    {
        stock = s;
        
        File file = new File("X:/光大证券/vipdoc/" + (stock.isShangHai ? "sh/lday/sh" : "sz/lday/sz") + pad(stock.number));//new File(DirUtils.getProjectRoot() + "../光大证券/vipdoc/" + (stock.isShangHai ? "sh/lday/sh" : "sz/lday/sz") + pad(stock.number));
        FileInputStream fin = new FileInputStream(file);
        //FileInputStream fin = new FileInputStream(file);

        long length = file.length();

        if (firstDay > 0)
        {
            fin.skip(length - (firstDay << 5));
        }

        while (lastDay <= 1 || size() <= lastDay - firstDay)
        {
            try
            {
                add(DateData.getNext(fin));
            }
            catch (IOException e)
            {
                if (lastDay < 0)
                {
                    return;
                }
                fin.reset();
                long firstday = getNextLong(fin);
                fin.reset();
                fin.skip(length - 8);
                long lastday = getNextLong(fin);

                throw new IOException("这支股票(" + stock.number + ")没有这么多的交易日期. 可能是因为数据没有下载全吧. 现有的交易日期: " + firstday + " - " + lastday);
            }
        }
    }

    private static long getNextLong(InputStream in) throws IOException
    {
        int ch = 0;
        long d = 0;

        for (int i = 0; i < 64; i += 8)
        {
            ch = in.read();
            if (ch < 0)
                throw new EOFException();

            d += ch << i;
        }
        return d;
    }

    float func(String method, List<Float> args)
    {
        switch (method)
        {
            case "min":
                return min(args.get(0).intValue(), args.get(1).intValue());

            case "max":
                return max(args.get(0).intValue(), args.get(1).intValue());

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
