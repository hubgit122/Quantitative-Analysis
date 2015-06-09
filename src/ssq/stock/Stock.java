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
import java.net.SocketTimeoutException;
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
    public static final String    stockListPath    = DirUtils.getXxRoot("assets/stockHistories");
    
    public boolean                isShangHai;
    private int                   number;
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
    
    public int getNumber()
    {
        return number;
    }
    
    public String getNumberString()
    {
        return pad(number);
    }
    
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
    
    public static final String filter       = "600.*|601.*|603.*|000.*|001.*|002.*|300.*";
    public static int          numOfThreads = 25;
    
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
                        return;
                    }

                    while (true)
                    {
                        if (getStatus() == 2)
                        {
                            break;
                        }
                        
                        try
                        {
                            stock.update();
                            break;
                        }
                        catch (UnknownHostException | NoRouteToHostException | SocketTimeoutException e)
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
    
    public void save() throws IOException
    {
        ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(FileUtils.assertFileExists(new File("assets/stockHistories", String.valueOf(pad(this.number)))))));
        o.writeObject(this);
        o.close();
    }

    private void update() throws IOException
    {
        try
        {
            String tmp = queryLatest();

            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

            if (tmp.length() < 100 || tmp.endsWith(",-2\";") || tmp.contains(format.format(DateData.numberToDate(history.getLastStoredDate()))))
            {
                return;
            }
            else
            {
                history.updateData();
            }
        }
        catch (Exception e)
        {
            history.updateData();
        }
    }

    String queryLatest() throws IOException
    {
        return StringUtils.convertStreamToString(FileUtils.downloadFile("http://hq.sinajs.cn/list=" + getCode()), "gb2312");
    }

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
