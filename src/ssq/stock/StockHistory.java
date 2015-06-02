package ssq.stock;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import ssq.stock.interpreter.ReflectTreeBuilder.ValueType;
import ssq.utils.MathUtils;

/**
 * 存放日线数据, 可以计算日线数据的函数<br>
 * 最近一天的数据是最后一个元素
 *
 * @author s
 */
public class StockHistory extends ArrayList<DateData>
{
    private static final long serialVersionUID = 1L;
    
    Stock                     stock;
    
    public StockHistory(Stock stock, File file, int firstDay, int lastDay) throws IOException
    {
        this.stock = stock;
        
        DataInputStream fin = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));

        long length = file.length();

        if (firstDay > 0)
        {
            fin.skip(length - (firstDay << 5));
        }
        
        DateData lastDateData = new DateData(0, 0, 0, 0, 0, 0, 0, 1f);

        while (lastDay <= 1 || size() <= lastDay - firstDay)
        {
            try
            {
                DateData tmp = DateData.getNext(fin, lastDateData.scale);
                
                if (tmp.vals[3] < MathUtils.round(0.9f * lastDateData.vals[3])) //今天是除权除息日
                {
                    float s = getScale(tmp.vals[0], lastDateData.vals[3], (float) lastDateData.vals[3] / tmp.vals[0]) * lastDateData.scale;
                    lastDateData = tmp.setScale(s);
                }
                else
                {
                    lastDateData = tmp;
                }
                
                add(lastDateData);
            }
            catch (IOException e)
            {
                if (lastDay < 0)
                {
                    return;
                }

                fin.reset();
                long firstday = MathUtils.getNextLong(fin);
                fin.reset();
                fin.skip(length - 8);
                long lastday = MathUtils.getNextLong(fin);

                throw new IOException("这支股票(" + stock.number + ")没有这么多的交易日期. 可能是因为数据没有下载全吧. 现有的交易日期: " + firstday + " - " + lastday);
            }
        }
        
        fin.close();
    }

    private boolean withinReach(int f1, int f2)
    {
        if ((MathUtils.round(f1 * 1.1f) > f2) && ((MathUtils.round(f1 * 0.9f) < f2)))
        {
            return true;
        }
        return false;
    }

    private float getScale(int thisDay, int lastDay, float std)
    {
        float result = std, minDiff = 100;
        
        for (float i = 1f; i < 10f; i++)
        {
            for (float j = 1f; j < i; j++)
            {
                float scale = i / j;
                float tmp = Math.abs(scale - std);
                
                if (tmp < minDiff && withinReach(MathUtils.round(lastDay / scale), thisDay))
                {
                    minDiff = tmp;
                    result = scale;
                }
            }
        }

        return result;
    }

    public float func(String method, List<Float> args, ValueType type, boolean rest)
    {
        switch (method)
        {
            case "min":
                return min(args.get(0).intValue() + args.get(2).intValue(), args.get(1).intValue() + args.get(2).intValue(), type, rest);

            case "max":
                return max(args.get(0).intValue() + args.get(2).intValue(), args.get(1).intValue() + args.get(2).intValue(), type, rest);
                
            case "sum":
                return sum(args.get(0).intValue() + args.get(2).intValue(), args.get(1).intValue() + args.get(2).intValue(), type, rest);
                
                //            case "mean":
                //                return mean(args.get(0).intValue() + args.get(2).intValue(), args.get(1).intValue() + args.get(2).intValue(), type, rest);

            case "avarage":
                return avarage(args.get(0).intValue() + args.get(2).intValue(), args.get(1).intValue() + args.get(2).intValue(), type, rest);

            default:
                throw new UnsupportedOperationException(method);
        }
    }
    
    private float avarage(int firstDay, int lastDay, ValueType type, boolean rest)
    {
        return sum(firstDay, lastDay, type, rest) / (firstDay - lastDay + 1);
    }
    
    //    private float mean(int firstDay, int lastDay, ValueType type, boolean rest)
    //    {
    //        return -1;
    //    }
    
    private float sum(int firstDay, int lastDay, ValueType type, boolean rest)
    {
        int result = 0;
        int size = size();

        for (int i = size - firstDay; i <= size - lastDay; i++)
        {
            int s = rest ? MathUtils.round(get(i).getScaledVal(type)) : get(i).vals[type.ordinal()];
            result += s;
        }
        return result;
    }
    
    private int max(int firstDay, int lastDay, ValueType type, boolean rest)
    {
        int result = Integer.MIN_VALUE;
        int size = size();

        for (int i = size - firstDay; i <= size - lastDay; i++)
        {
            int s = rest ? MathUtils.round(get(i).getScaledVal(type)) : get(i).vals[type.ordinal()];
            
            if (s > result)
            {
                result = s;
            }
        }
        return result;
    }
    
    private int min(int firstDay, int lastDay, ValueType type, boolean rest)
    {
        int result = Integer.MAX_VALUE;
        int size = size();

        for (int i = size - firstDay; i <= size - lastDay; i++)
        {
            int s = rest ? MathUtils.round(get(i).getScaledVal(type)) : get(i).vals[type.ordinal()];
            
            if (s < result)
            {
                result = s;
            }
        }
        return result;
    }
}
