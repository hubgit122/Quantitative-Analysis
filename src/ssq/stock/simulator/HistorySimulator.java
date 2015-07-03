package ssq.stock.simulator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;

import ssq.stock.Stock;
import ssq.stock.simulator.Deal.Type;
import ssq.utils.FileUtils;
import ssq.utils.Pair;

/**
 * 使用历史分笔交易数据制作的交易模拟器, 通过委托交易的方式模拟成交, 每秒做一次决定<br>
 * 可以选择跳跃多少时间, 不可以设置价格触发器. 价格触发器应该是交易代码的一部分
 *
 * @author s
 */
public class HistorySimulator extends Simulator
{
    private static final long                                 serialVersionUID = 1L;
    /**
     * 指定股票代码和日期, 缓存该股票当天的交易数据
     */
    static Hashtable<Pair<Integer, String>, LinkedList<Deal>> mem              = new Hashtable<>();

    /**
     * 模拟器的当前时间
     */
    Calendar                                                  simulationNow;
    /**
     * 缓存的当前时间的年月日
     */
    int                                                       year, month, date;

    public HistorySimulator(String name) throws Exception
    {
        super(name);
    }
    
    /**
     * @return 最近成交价
     *
     * @throws IOException
     */
    @Override
    public Float getCurrentPrice(int code) throws IOException
    {
        LinkedList<Deal> tmp = getData(code);

        float currentPrice = 0;
        Date time = simulationNow.getTime();

        while (tmp.size() > 0)
        {
            if (tmp.getFirst().getTime().before(time))
            {
                currentPrice = tmp.removeFirst().getPrice();
            }
            else
            {
                break;
            }
        }
        
        return currentPrice;
    }

    /**
     * 从历史数据表里读取数据, 如果没有, 则去网上下载
     *
     * @param code
     * @return
     * @throws IOException
     */
    public LinkedList<Deal> getData(int code) throws IOException
    {
        Pair<Integer, String> key = new Pair<>(code, dateFormat.format(simulationNow.getTime()));
        LinkedList<Deal> tmp = mem.get(key);
        if (tmp == null)
        {
            LinkedList<Deal> deals = parseDeals(getDeals(key));
            mem.put(key, deals);
            tmp = deals;
        }
        return tmp;
    }
    
    /**
     * 将下载的交易记录解析成内部格式
     *
     * @param deals
     * @return
     */
    private LinkedList<Deal> parseDeals(InputStream in)
    {
        LinkedList<Deal> result = new LinkedList<>();

        BufferedReader reader = null;
        try
        {
            reader = new BufferedReader(new InputStreamReader(in, "GBK"));
        }
        catch (UnsupportedEncodingException e2)
        {
        }
        
        try
        {
            reader.readLine();
            
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                String[] tmp = line.split("\t");
                
                try
                {
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTime(timeFormat.parse(tmp[0]));
                    calendar.set(year, month, date);

                    result.addFirst(new Deal(calendar.getTime(), Float.parseFloat(tmp[1]), Integer.parseInt(tmp[3]) * 100, Type.OTHERS, -1));
                }
                catch (ParseException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (IOException e1)
        {
        }
        
        try
        {
            reader.close();
        }
        catch (IOException e)
        {
        }
        return result;
    }
    
    /**
     * 下载交易记录
     *
     * @throws IOException
     */
    private static InputStream getDeals(Pair<Integer, String> key) throws IOException
    {
        int code = key.getKey();
        String date = key.getValue();
        return FileUtils.downloadFile("http://market.finance.sina.com.cn/downxls.php?date=" + date + "&symbol=" + Stock.getCodeWithAddr(code));
    }

    /**
     * 让时间继续进行
     *
     * @param days
     * @param seconds
     */
    public void goAhead(int days, int seconds)
    {
        simulationNow.add(Calendar.DATE, days);
        simulationNow.add(Calendar.SECOND, seconds);

        refreshCache();
    }
    
    private void refreshCache()
    {
        year = simulationNow.get(Calendar.YEAR);
        month = simulationNow.get(Calendar.MONTH);
        date = simulationNow.get(Calendar.DATE);
    }
    
    public void setDate(int y, int m, int d)
    {
        year = y;
        month = m;
        date = d;
        
        simulationNow.set(y, m, d);
    }
    
    @Override
    public boolean commit(Account account, int code, float price, boolean buy, int quantity)
    {
        // TODO 自动生成的方法存根
        return false;
    }

    @Override
    public void save()
    {
        // TODO 自动生成的方法存根
        
    }

    @Override
    protected void onNewTime()
    {
        // TODO 自动生成的方法存根
        
    }
}