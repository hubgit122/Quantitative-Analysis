package ssq.stock;

import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ssq.stock.gui.GUI;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import ssq.utils.StringUtils;
import ssq.utils.taskdistributer.Task;
import ssq.utils.taskdistributer.TaskDistributor;
import ssq.utils.taskdistributer.TaskList;
import ssq.utils.taskdistributer.WorkThread;

public class Stock implements Serializable
{
    private static final long     serialVersionUID = 3937169104043760931L;
    
    public static final StockList stockList        = new StockList();
    /**
     * 存放股票历史日线数据的目录
     */
    public static final String    stockListPath    = DirUtils.getXxRoot("assets/stockHistories");
    
    /**
     * 是否是上交所股票(否则是深交所)
     */
    public boolean                isShangHai;
    /**
     * 代码
     */
    private int                   number;
    /**
     * 历史日线数据
     */
    public StockHistory           history;
    /**
     * 股票名字
     */
    transient public String       name;
    /**
     * 当前是否停牌(退市或暂时停牌)
     */
    transient boolean             isStop           = false;
    
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
    public Stock(String name, boolean isShangHai, int number)
    {
        this.name = name;
        this.isShangHai = isShangHai;
        this.number = number;
        history = new StockHistory(this);
    }
    
    public int getNumber()
    {
        return number;
    }

    /**
     * 将不足6位的代码前面补0, 得到规整的股票代码
     *
     * @return
     */
    public String getNumberString()
    {
        return pad(number);
    }
    
    /**
     * 上交所的加前缀sh, 深交所的加前缀sz
     *
     * @return
     */
    public String getCode()
    {
        return (isShangHai ? "sh" : "sz") + getNumberString();
    }
    
    @Override
    public String toString()
    {
        return getCode() + name;
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
    
    //加载Stock类的时候重新加载股票代码->名称列表
    static
    {
        try
        {
            readList();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 股票代码的过滤器
     */
    public static final String filter       = "600.*|601.*|603.*|000.*|001.*|002.*|300.*";
    /**
     * 默认的下载线程数
     */
    public static int          numOfThreads = 25;
    
    /**
     * 采用多线程更新所有股票的历史数据
     *
     * @throws Exception
     */
    public static void updateStocks() throws Exception
    {
        try
        {
            numOfThreads = Integer.valueOf(GUI.getInstance().textFields[4].getText());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        GUI.statusText("开始更新股票信息");
        
        final int cnt = stockList.size();
        
        final TaskList taskList = new TaskList();
        final TaskDistributor distributor = new TaskDistributor(taskList, numOfThreads, WorkThread.class)
        {
            private LinkedList<Exception> exceptions = new LinkedList<Exception>();
            
            {
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        while (exceptions != null)
                        {
                            try
                            {
                                if (!exceptions.isEmpty())
                                {
                                    int index = Integer.MAX_VALUE;
                                    
                                    synchronized (exceptions)
                                    {
                                        for (Exception exception : exceptions)
                                        {
                                            index = Math.min(((DownloadException) exception).index, index);
                                        }
                                    }
                                    
                                    Toolkit.getDefaultToolkit().beep();
                                    
                                    final int ind = index;
                                    try
                                    {
                                        SwingUtilities.invokeAndWait(new Runnable()
                                        {
                                            @Override
                                            public void run()
                                            {
                                                int msg = JOptionPane.showConfirmDialog(GUI.instance, "请确保网络畅通, 下载会自动开始. 点击NO终止本次下载. ", "貌似断网了", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                                                if (msg == JOptionPane.CLOSED_OPTION | msg == JOptionPane.NO_OPTION)
                                                {
                                                    synchronized (taskList)
                                                    {
                                                        for (Task task : taskList)
                                                        {
                                                            abort(task.getTaskId());
                                                        }
                                                    }
                                                    
                                                    GUI.statusText("更新部分完成. 从" + stockList.get(ind) + "开始更新失败. ");
                                                }
                                            }
                                        });
                                    }
                                    catch (InvocationTargetException e)
                                    {
                                        e.printStackTrace();
                                    }
                                    catch (InterruptedException e)
                                    {
                                        e.printStackTrace();
                                    }
                                    
                                    exceptions.clear();
                                }
                            }
                            catch (NullPointerException e)
                            {
                            }

                            try
                            {
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException e)
                            {
                            }
                            
                        }
                    }
                }).start();
            }

            @Override
            public void waitTasksDone()
            {
                super.waitTasksDone();

                exceptions = null;

                GUI.statusText("股票信息更新完毕");
            }

            @Override
            public Task getNext(int lastFinished)
            {
                Task result = super.getNext(lastFinished);
                if (lastFinished >= 0)
                    GUI.statusText(stockList.get(lastFinished) + "更新完毕, 进度: " + getProgress() + "%");
                return result;
            }
            
            @Override
            public void informException(Exception e)
            {
                synchronized (exceptions)
                {
                    exceptions.add(e);
                }
            }
        };
        
        for (int i = 0; i < cnt; i++)
        {
            taskList.add(new Task(i)
            {
                @Override
                public void execute()
                {
                    Stock stock;
                    try
                    {
                        stock = loadStock(stockList.get(getTaskId()).getKey());
                    }
                    catch (IOException e1)
                    {
                        e1.printStackTrace();
                        stock = new Stock(stockList.get(getTaskId()).getValue(), stockList.get(getTaskId()).getKey() > 599999, stockList.get(getTaskId()).getKey());
                    }

                    while (true)
                    {
                        if (getStatus() == Task.ABROTED)
                        {
                            break;
                        }
                        
                        try
                        {
                            stock.update();
                            break;
                        }
                        catch (UnknownHostException | NoRouteToHostException e)
                        {
                            if ((getStatus() & Task.FINISHED) != 0)
                            {
                                break;
                            }

                            distributor.informException(new DownloadException(e, getTaskId()));
                            try
                            {
                                Thread.sleep(1000);
                            }
                            catch (InterruptedException e1)
                            {
                            }
                        }
                        catch (Exception e2)
                        {
                        }
                    }

                    try
                    {
                        stock.save();
                    }
                    catch (IOException e)
                    {
                        distributor.informException(e);
                    }
                }
            });
        }
        
        distributor.schedule();
        
        new Thread()
        {
            @Override
            public void run()
            {
                distributor.waitTasksDone();
            };
        }.start();
    }

    /**
     * 从磁盘加载上次缓存的代码为index的股票历史数据
     *
     * @param index
     * @return
     * @throws IOException
     */
    public static Stock loadStock(int index) throws IOException
    {
        Stock result = null;
        ObjectInputStream i = null;
        
        try
        {
            i = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File("assets/stockHistories", Stock.pad(index)))));
            result = (Stock) i.readObject();
            result.reconstruct(stockList.getNameOfId(index));
        }
        catch (Exception e)
        {
            result = new Stock(stockList.getNameOfId(index), index > 400000, index);
        }
        finally
        {
            try
            {
                i.close();
            }
            catch (Exception e)
            {
            }
        }

        return result;
    }

    /**
     * 从磁盘读取数据集后, 把股票名称重构出来, 并重构历史数组的反向引用
     *
     * @param name
     */
    private void reconstruct(String name)
    {
        this.name = name;
        this.history.reconstruct(this);
    }

    /**
     * 下载失败时产生的异常, 用于保存失败的股票的索引
     * 
     * @author s
     */
    public static class DownloadException extends Exception
    {
        private static final long serialVersionUID = -3732234200535226400L;
        int                       index;

        public DownloadException(Exception exception, int index)
        {
            super(exception);
            this.index = index;
        }
    }
    
    /**
     * 保存当前股票的历史
     *
     * @throws IOException
     */
    public void save() throws IOException
    {
        ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(FileUtils.assertFileExists(new File("assets/stockHistories", String.valueOf(pad(this.number)))))));
        o.writeObject(this);
        o.close();
    }

    /**
     * 更新历史数据
     *
     * @throws IOException
     */
    private void update() throws IOException
    {
        try
        {
            String tmp = queryLatest();

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            if (tmp.length() < 100 || tmp.endsWith(",-2\";") || tmp.indexOf(',') == -1 || Float.valueOf(tmp.split(",")[1]) == 0f || tmp.contains(format.format(DateData.numberToDate(history.getLastStoredDate()))))
            {
                return;
            }
        }
        catch (Exception e)
        {
        }
        history.updateData();
    }
    
    /**
     * 向新浪查询最近的交易数据, 用以判断是否停牌
     *
     * @return
     * @throws IOException
     */
    public String queryLatest() throws IOException
    {
        return StringUtils.convertStreamToString(FileUtils.downloadFile("http://hq.sinajs.cn/list=" + getCode()), "gb2312");
    }

    /**
     * 先尝试从东方财富网上更新股票的代码->名称列表, 如果失败, 则读取上次缓存的列表
     */
    public static void readList()
    {
        String tmp;
        
        try
        {
            try
            {
                String url = "http://quote.eastmoney.com/stocklist.html";
                tmp = StringUtils.convertStreamToString(FileUtils.downloadFile(url), "gb2312");

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
                                stockList.add(insertIndex, new IDNamePair(id, name));
                            }
                        }
                    }
                }

                ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(new File(stockListPath, "list"))));
                o.writeObject(stockList);
                o.close();
            }
            catch (Exception e)
            {
                JOptionPane.showMessageDialog(null, "连接股票列表服务器失败, 下面尝试上次缓存的结果. 也可以尝试重新连接网络并重启程序. ");

                try
                {
                    ObjectInputStream i = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(stockListPath, "list"))));
                    stockList.addAll((StockList) i.readObject());
                    i.close();
                }
                catch (Exception e1)
                {
                }

            }
            
        }
        catch (Exception e)
        {
            JOptionPane.showMessageDialog(null, "没有网络也没有缓存的股票列表, 程序初始化失败");
        }
    }

}
