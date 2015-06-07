package ssq.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import ssq.stock.interpreter.ReflectTreeBuilder.ValueType;
import ssq.utils.FileUtils;
import ssq.utils.StringUtils;

/**
 * 存放日线数据, 可以计算日线数据的函数<br>
 * 最近一天的数据是最后一个元素
 *
 * @author s
 */
public class StockHistory extends ArrayList<DateData>
{
    private static final long serialVersionUID = -5527331443161318429L;
    transient Stock           stock;
    
    public StockHistory(Stock stock) throws IOException
    {
        updateData(stock);
    }
    
    public void updateData(Stock stock) throws IOException
    {
        this.stock = stock;
        
        Date now = Calendar.getInstance().getTime();
        int lastDate = -1;
        
        if (now.getHours() < 15)
        {
            now.setTime(now.getTime() - 24 * 3600 * 1000);
        }
        lastDate = DateData.dateToNumber(now);
        
        int lastStoredDate;
        try
        {
            lastStoredDate = get(size() - 1).date;
        }
        catch (IndexOutOfBoundsException e)
        {
            lastStoredDate = -1;
        }
        
        if (lastStoredDate == lastDate)
        {
            return;
        }
        
        Date nextDateToStore = new Date(DateData.numberToDate(lastStoredDate).getTime() + 24 * 3600 * 1000);

        for (int year = lastStoredDate == -1 ? 2010 : nextDateToStore.getYear() + 1900; year <= now.getYear() + 1900; year++)
        {
            for (int season = lastStoredDate == -1 ? 1 : (nextDateToStore.getMonth() + 3) / 3; season <= (year == now.getYear() + 1900 ? (now.getMonth() + 3) / 3 : 4); season++)
            {
                String url = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/" + Stock.pad(stock.number) + ".phtml?year=" + year + "&jidu=" + season;
                Document doc = Jsoup.parse(StringUtils.convertStreamToString(FileUtils.downloadFile(url), "gb2312"));
                Element table = doc.getElementById("FundHoldSharesTable");
                Elements records = table.getElementsByTag("tbody");
                if (records.size() == 0)
                {
                    records = table.children();
                    records.remove(0);
                }
                else
                {
                    records = records.get(0).children();
                }
                
                for (int i = records.size() - 1; i > 0; --i) // 第一行是表头
                {
                    List<Element> nodes = records.get(i).children(); //一行的数据
                    
                    int date = parseDate(nodes.get(0).child(0).text()); //有可能没有链接
                    if (isEmpty() || date > get(size() - 1).date)
                    {
                        nodes.remove(0);
                        float vals[] = new float[7];
                        int j = 0;
                        for (Element node : nodes)
                        {
                            Element tmp = node.child(0);
                            vals[j++] = Float.valueOf(tmp.html());
                        }
                        
                        add(new DateData(date, vals));
                    }
                }
            }
        }
    }
    
    private static int parseDate(String dateString)
    {
        return Integer.valueOf(dateString.replaceAll("-", ""));
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
        float result = 0;
        int size = size();

        for (int i = size - firstDay; i <= size - lastDay; i++)
        {
            float s = rest ? get(i).getScaledVal(type) : get(i).getVal(type);
            result += s;
        }
        return result;
    }
    
    private float max(int firstDay, int lastDay, ValueType type, boolean rest)
    {
        float result = Float.MIN_VALUE;
        int size = size();

        for (int i = size - firstDay; i <= size - lastDay; i++)
        {
            float s = rest ? get(i).getScaledVal(type) : get(i).getVal(type);
            
            if (s > result)
            {
                result = s;
            }
        }
        return result;
    }
    
    private float min(int firstDay, int lastDay, ValueType type, boolean rest)
    {
        float result = Float.MAX_VALUE;
        int size = size();

        for (int i = size - firstDay; i <= size - lastDay; i++)
        {
            float s = rest ? get(i).getScaledVal(type) : get(i).getVal(type);
            
            if (s < result)
            {
                result = s;
            }
        }
        return result;
    }
}
