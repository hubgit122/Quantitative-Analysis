package ssq.stock.analyser;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import ssq.stock.DateData;
import ssq.stock.Stock;
import ssq.stock.StockHistory;
import ssq.utils.FileUtils;
import ssq.utils.Pair;
import ssq.utils.StringUtils;

public class IntegralityAnalyzer extends Analyzer
{
    Set<Pair<Integer, Pair<Integer, Integer>>> empty = new HashSet<>();

    public static void main(String[] args) throws Exception
    {
        new IntegralityAnalyzer().run();
    }

    @Override
    public void run() throws Exception
    {
        try
        {
            ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream("assets/stockHistories/emptySeasonCache"));
            empty = (Set<Pair<Integer, Pair<Integer, Integer>>>) inputStream.readObject();
            inputStream.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        super.run();
    }

    private ReadWriteLock myLock = new ReentrantReadWriteLock(false);

    public void saveEmptyList() throws IOException, FileNotFoundException
    {
        ObjectOutputStream outputStream;
        try
        {
            outputStream = new ObjectOutputStream(new FileOutputStream("assets/stockHistories/emptySeasonCache"));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return;
        }
        
        myLock.readLock().lock();
        try
        {
            outputStream.writeObject(empty);
        }
        catch (Exception e)
        {
        }
        finally
        {
            myLock.readLock().unlock();
            outputStream.close();
        }
    }

    @Override
    public void scan(Stock stock) throws IOException
    {
        Date lastDownloadableDate = stock.history.getLastDownloadableDate();

        for (int year = 2010; year <= lastDownloadableDate.getYear() + 1900; year++)
        {
            for (int season = 1; season <= (year == lastDownloadableDate.getYear() + 1900 ? (lastDownloadableDate.getMonth() + 3) / 3 : 4); season++)
            {
                if (!haveDataInYearAndSeason(stock.history, year, season))
                {
                    String url = "http://vip.stock.finance.sina.com.cn/corp/go.php/vMS_FuQuanMarketHistory/stockid/" + stock.getCodeString() + ".phtml?year=" + year + "&jidu=" + season;
                    Pair<Integer, Pair<Integer, Integer>> tmp = new Pair<Integer, Pair<Integer, Integer>>(stock.getCode(), new Pair<>(year, season));

                    myLock.readLock().lock();
                    boolean flag = empty.contains(tmp);
                    myLock.readLock().unlock();

                    if (flag || StringUtils.convertStreamToString(FileUtils.downloadFile(url), "gb2312").indexOf("<a target='_blank' href=") == -1)
                    {
                        if (!flag)
                        {
                            myLock.writeLock().lock();
                            empty.add(tmp);
                            myLock.writeLock().unlock();
                            saveEmptyList();
                        }
                    }
                    else
                    {
                        System.out.println(stock + "的" + year + "年" + season + "季度的交易信息被遗漏");
                        stock.history.clear();
                        stock.history.updateData();
                        stock.save();
                        System.out.println(stock + "的交易信息已修复");
                    }
                }
            }
        }
    }

    private boolean haveDataInYearAndSeason(StockHistory history, int year, int season)
    {
        for (DateData dateData : history)
        {
            int seasonStart = year * 10000 + (season * 3 - 2) * 100;
            int nextSeasonStart = year * 10000 + (season * 3 + 1) * 100;

            if (dateData.date > seasonStart && dateData.date < nextSeasonStart)
            {
                return true;
            }
        }
        return false;
    }
}
