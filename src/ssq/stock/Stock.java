package ssq.stock;

import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.SocketTimeoutException;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import ssq.stock.gui.GUI;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import ssq.utils.Pair;
import ssq.utils.StringUtils;

public class Stock implements Serializable
{
    private static final long     serialVersionUID = 3937169104043760931L;
    final public static StockList stockList        = new StockList();
    public static final String    stockListPath    = DirUtils.getXxRoot("assets/stockHistories");
    static boolean                integral         = true;
    public boolean                isShangHai;
    public int                    number;
    public StockHistory           history;
    transient public String       name;

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
    public Stock(String name, boolean isShangHai, int number) throws IOException
    {
        this.name = name;
        this.isShangHai = isShangHai;
        this.number = number;
        history = new StockHistory(this);
    }
    
    @Override
    public String toString()
    {
        return (isShangHai ? "sh" : "sz") + name + pad(number);
    }

    public static String pad(String s)
    {
        StringBuilder sb = new StringBuilder(6);

        int end = 6 - s.length();
        for (int j = 0; j < end; j++)
        {
            sb.append('0');
        }
        sb.append(s);

        return sb.toString();
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

    static
    {
        try
        {
            readList();

            if (JOptionPane.showConfirmDialog(null, "更新数据会花费大量的时间, 请保持网络畅通并关注状态条上的进度提示. ", "更新股票数据? ", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION)
            {
                updateStocks();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static final String filter = "600.*|601.*|603.*|000.*|001.*|002.*|300.*";

    public static void updateStocks() throws Exception
    {
        try
        {
            GUI.statusText("开始更新股票信息");

            int numOfThreads = 1;
            final LinkedList<DownloadException> exceptions = new LinkedList<>();
            //            ArrayList<DownloadAndSave> threads = new ArrayList<>(numOfThreads);
            int cnt = stockList.size();
            
            for (int i = 0; i < cnt; i += numOfThreads)
            {
                if (exceptions.isEmpty())
                {
                    //                    threads.clear();
                    for (int j = 0; j < numOfThreads; j++)
                    {
                        int index = j + i;
                        
                        if (index < cnt)
                        {
                            final Stock stock = loadStock(stockList.get(index).getKey());
                            GUI.statusText("正在更新 " + stock + ", 进度: " + 100.0 * i / cnt + "%");
                            
                            //                            threads.add(new DownloadAndSave(i + j)
                            //                            {
                            //                                @Override
                            //                                public void run()
                            //                                {
                            while (true)
                            {
                                try
                                {
                                    //                                Stock stock = loadStock(stockList.get(index).getKey());
                                    stock.update();
                                    stock.save();
                                    break;
                                }
                                catch (SocketTimeoutException e)
                                {
                                    Toolkit.getDefaultToolkit().beep();
                                    e.printStackTrace();
                                    synchronized (exceptions)
                                    {
                                        exceptions.add(new DownloadException(e, index));
                                    }
                                }
                                catch (Exception e)
                                {
                                }
                            }
                            //                                }
                            //                            });
                            //                            threads.get(j).start();
                        }
                    }
                    
                    //                    for (int j = 0; j < threads.size(); j++)
                    //                    {
                    //                        threads.get(j).join();
                    //                    }
                }
                else if (JOptionPane.showConfirmDialog(null, "请确保网络畅通后点击YES重试. ", "貌似断网了", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.NO_OPTION)
                {
                    GUI.statusText("更新部分完成. 更新完成的股票数: " + exceptions.getFirst().index);
                    break;
                }
                else
                {
                    for (DownloadException downloadException : exceptions)
                    {
                        i = Math.max(Math.min(i, downloadException.index) - numOfThreads, 0);
                    }
                    exceptions.clear();
                }
            }
            
            GUI.statusText("股票信息更新完毕");
        }
        catch (Exception e)
        {
            if (JOptionPane.showConfirmDialog(null, "如果需要联网更新股票名称并更新股票数据, 请先恢复网络再点击NO. ", "网络无法连接, 使用离线数据? ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
            {
                updateStocks();
            }
        }
    }

    public static Stock loadStock(int index) throws FileNotFoundException, IOException
    {
        ObjectInputStream i = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(stockListPath, Stock.pad(index)))));
        Stock result = null;
        try
        {
            result = (Stock) i.readObject();
            result.reconstruct(stockList.getNameOfId(index));
        }
        catch (ClassNotFoundException e)
        {
            e.printStackTrace();
        }
        i.close();
        return result;
    }
    
    private void reconstruct(String name)
    {
        this.name = name;
        this.history.reconstruct(this);
    }
    
    public static class DownloadException extends Exception
    {
        int index;
        
        public DownloadException(Exception exception, int index)
        {
            super(exception);
            this.index = index;
        }
    }

    public static class DownloadAndSave extends Thread
    {
        int index;
        
        DownloadAndSave(int i)
        {
            index = i;
        }
    }
    
    public void save() throws IOException
    {
        ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(FileUtils.assertFileExists(new File(stockListPath, String.valueOf(pad(this.number)))))));
        o.writeObject(this);
        o.close();
    }
    
    private void update() throws IOException
    {
        if (!history.isEmpty() || StringUtils.convertStreamToString(FileUtils.downloadFile("http://hq.sinajs.cn/list=s" + (isShangHai ? 'h' : 'z') + pad(number)), "gb2312").length() > 100)
        {
            history.updateData();
        }
    }
    
    public static void readList()
    {
        String tmp;

        try
        {
            String url = "http://quote.eastmoney.com/stocklist.html";
            tmp = StringUtils.convertStreamToString(FileUtils.downloadFile(url), "gb2312");
            
            try
            {
                FileWriter writer = new FileWriter(new File(DirUtils.getXxRoot("assets"), "stocklist.html"));
                writer.write(tmp);
                writer.close();
            }
            catch (Exception e)
            {
            }
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "连接股票列表服务器失败, 下面尝试上次缓存的结果. 也可以尝试重新连接网络并重启程序. ");
            tmp = FileUtils.fileToString(new File(DirUtils.getXxRoot("assets"), "stocklist.html"), "gb2312");
        }

        int offset = tmp.indexOf("<div class=\"sltit\"><a name=\"sz\"/>深圳股票</div>");

        Pattern p = Pattern.compile("<li><a target=\"_blank\" href=\"http://quote\\.eastmoney\\.com/s.\\d{6}\\.html\">([^\\(]+)\\((\\d{6})\\)</a></li>");

        for (int place = 0; place < 2; place++)
        {
            Matcher m = place == 1 ? p.matcher(tmp.substring(0, offset)) : p.matcher(tmp.substring(offset));

            while (m.find())
            {
                String num = m.group(2);
                
                if (num.matches(filter))
                {
                    String name = m.group(1);
                    Integer id = Integer.valueOf(num);
                    
                    int insertIndex = stockList.findInsertIndex(id);
                    
                    if (insertIndex >= 0)
                    {
                        stockList.add(insertIndex, new Pair<Integer, String>(id, name));
                    }
                }
            }

        }
    }
}
