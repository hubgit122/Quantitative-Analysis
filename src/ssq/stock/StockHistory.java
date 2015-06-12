package ssq.stock;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    
    public StockHistory(Stock stock)
    {
        this.stock = stock;
    }
    
    public void updateData() throws IOException
    {
        int lastStoredDate = getLastStoredDate();

        Date lastDownloadableDate = getLastDownloadableDate();

        if (lastStoredDate == DateData.dateToNumber(lastDownloadableDate))
        {
            return;
        }

        Date nextDateToStore = new Date(DateData.numberToDate(lastStoredDate).getTime() + 24 * 3600 * 1000);
        
        Pattern p = Pattern.compile("[^\\d]*([-\\d.]*)");

        for (int year = lastStoredDate == -1 ? 2010 : nextDateToStore.getYear() + 1900; year <= lastDownloadableDate.getYear() + 1900; year++)
        {
            for (int season = lastStoredDate == -1 ? 1 : (nextDateToStore.getMonth() + 3) / 3; season <= (year == lastDownloadableDate.getYear() + 1900 ? (lastDownloadableDate.getMonth() + 3) / 3 : 4); season++)
            {
                LinkedList<DateData> tmp = new LinkedList<>();

                String url = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/" + stock.getNumberString() + ".phtml?year=" + year + "&jidu=" + season;
                String content = StringUtils.convertStreamToString(FileUtils.downloadFile(url), "gb2312");
                
                int start = content.indexOf("<a target='_blank' href="), end = content.indexOf("<!--历史交易end-->");
                try
                {
                    content = content.substring(start, end);
                }
                catch (Exception e)
                {
                    continue;
                }
                
                for (int i = 0; i >= 0; i = content.indexOf("<a target='_blank' href="))
                {
                    content = content.substring(i + 136);

                    Matcher m = p.matcher(content);
                    
                    m.find();
                    int date = Integer.valueOf(m.group(1).replace("-", ""));
                    
                    if (date > lastStoredDate)
                    {
                        float[] vals = new float[7];
                        for (int j = 0; j < 7; j++)
                        {
                            m.find();
                            vals[j] = Float.valueOf(m.group(1));
                        }

                        tmp.addFirst(new DateData(date, vals));
                    }
                }
                
                addAll(tmp);
            }
        }
    }
    
    public Date getLastDownloadableDate()
    {
        Date lastDownloadableDate = Calendar.getInstance().getTime();

        if (lastDownloadableDate.getHours() < 19)
        {
            lastDownloadableDate.setTime(lastDownloadableDate.getTime() - 24 * 3600 * 1000);
        }
        return lastDownloadableDate;
    }
    
    public int getLastStoredDate()
    {
        int lastStoredDate;
        try
        {
            lastStoredDate = get(size() - 1).date;
        }
        catch (IndexOutOfBoundsException e)
        {
            lastStoredDate = -1;
        }
        return lastStoredDate;
    }

    private static int parseDate(String dateString)
    {
        return Integer.valueOf(dateString.replaceAll("-", ""));
    }

    public float func(String method, List<Float> args, ValueType type, boolean rest)
    {
        int backDays = args.get(2).intValue();
        
        if (backDays > 19000000) //是个日期
        {
            if (backDays > DateData.dateToNumber(getLastDownloadableDate()))
            {
                System.err.println("你太着急了, " + backDays + "当天的数据还不能被完全获得");
                backDays = 0;
            }
            else
            {
                int i = size() - 1;
                for (; i >= 0; --i)
                {
                    if (get(i).date <= backDays)
                    {
                        break;
                    }
                }

                if (i < 0)
                {
                    System.err.println(stock + "的历史数据太少, 不能支持回溯到" + backDays);
                }

                backDays = size() - 1 - i;
            }
        }
        
        switch (method)
        {
            case "min":
                return min(args.get(0).intValue() + backDays, args.get(1).intValue() + backDays, type, rest);
                
            case "max":
                return max(args.get(0).intValue() + backDays, args.get(1).intValue() + backDays, type, rest);

            case "sum":
                return sum(args.get(0).intValue() + backDays, args.get(1).intValue() + backDays, type, rest);

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

    public void reconstruct(Stock stock)
    {
        this.stock = stock;
    }
    
    public void trim()
    {
        int size = size();
        DateData lastDate = new DateData(-1, new float[] {});
        int cnt = 0;
        
        for (int i = 0; i < size - 1; i++)
        {
            DateData thisDate = get(i);

            if (thisDate.date < lastDate.date)
            {
                System.out.println(this.stock + "reduantant");
                removeRange(i, size - 1);
                break;
            }
            
            if (thisDate.vals.equals(lastDate.vals))
            {
                if (cnt < 3)
                {
                    cnt++;
                }
                else
                {
                    System.out.println(this.stock + "identical");
                    this.clear();
                    break;
                }
            }
            else
            {
                cnt = 0;
            }
            lastDate = thisDate;
        }
    }
}
