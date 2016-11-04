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
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import ssq.stock.analyser.ReflectTreeBuilder.ValueType;
import ssq.stock.gui.GUI;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import ssq.utils.StringUtils;
import ssq.utils.taskdistributer.Task;
import ssq.utils.taskdistributer.TaskDistributor;
import ssq.utils.taskdistributer.TaskList;

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
  
  public Stock(String name, int code)
  {
    this.name = name;
    this.isShangHai = code >= 600000;
    this.number = code;
    history = new StockHistory(this);
  }
  
  public int getCode()
  {
    return number;
  }
  
  /**
   * 将不足6位的代码前面补0, 得到规整的股票代码
   */
  public static String getCodeString(int code)
  {
    return pad(code);
  }
  
  public String getCodeString()
  {
    return getCodeString(number);
  }

  public boolean isLimitedRiseAtTheBegining(int index) {
    if (index<=0)
    {
      return false;
    }

    DateData thisDay = history.get(index) , lastDay = history.get(index-1);

    return lastDay.getScaledVal(ValueType.closing) * 1.1 <= thisDay.getScaledVal(ValueType.opening) + 0.01;
  }

  /**
   * 上交所的加前缀sh, 深交所的加前缀sz
   */
  public static String getCodeWithAddr(int code)
  {
    return (code >= 600000 ? "sh" : "sz") + getCodeString(code);
  }
  
  private String getCodeWithAddr()
  {
    return getCodeWithAddr(number);
  }
  
  @Override
  public String toString()
  {
    return getCodeWithAddr() + name;
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
  
  // 加载Stock类的时候重新加载股票代码->名称列表
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
    final TaskDistributor distributor = new TaskDistributor(taskList, numOfThreads)
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
            stock = loadDayLineHistory(stockList.get(getTaskId()).getKey());
          }
          catch (IOException e1)
          {
            e1.printStackTrace();
            stock = new Stock(stockList.get(getTaskId()).getValue(), stockList.get(getTaskId()).getKey());
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
            catch (SocketTimeoutException | SocketException e3)
            {
            }
            catch (Exception e2)
            {
              e2.printStackTrace();
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
   * @param number
   * @throws IOException
   */
  public static Stock loadDayLineHistory(int code) throws IOException
  {
    Stock result = null;
    ObjectInputStream i = null;
    
    try
    {
      i = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File("assets/stockHistories", Stock.pad(code)))));
      result = (Stock) i.readObject();
      result.loadName();
    }
    catch (Exception e)
    {
      result = new Stock(stockList.getNameOfId(code), code);
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
  
  private void loadName()
  {
    reconstruct(stockList.getNameOfId(number));
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
  
  SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
  
  /**
   * 更新历史数据
   *
   * @throws IOException
   */
  public void update() throws IOException
  {
    try
    {
      String tmp = queryLatest();
      
      if (tmp.length() < 100 || tmp.endsWith(",-2\";") || tmp.indexOf(',') == -1 || tmp.contains(format.format(DateData.numberToDate(history.getLastStoredDate()))))
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
   * 向新浪查询最近的交易数据, 用以判断是否停牌 <br>
   * 数据例:<br>
   * var hq_str_sh601006= "大秦铁路, 27.55, 27.25, 26.91, 27.55, 26.20, 26.91, 26.92, 22114263, 589824680, 4695, 26.91, 57590, 26.90, 14700, 26.89, 14300, 26.88, 15100, 26.87, 3100, 26.92, 8900, 26.93, 14230, 26.94, 25150, 26.95, 15220, 26.96, 2008-01-11, 15:05:32" ;<br>
   * 0：”大秦铁路”，股票名字<br>
   * 1：”27.55″，今日开盘价<br>
   * 2：”27.25″，昨日收盘价<br>
   * 3：”26.91″，当前价格<br>
   * 4：”27.55″，今日最高价<br>
   * 5：”26.20″，今日最低价<br>
   * 6：”26.91″，竞买价，即“买一”报价<br>
   * 7：”26.92″，竞卖价，即“卖一”报价<br>
   * 8：”22114263″，成交的股票数，由于股票交易以一百股为基本单位，所以在使用时，通常把该值除以一百<br>
   * 9：”589824680″，成交金额，单位为“元”，为了一目了然，通常以“万元”为成交金额的单位，所以通常把该值除以一万<br>
   * 10：”4695″，“买一”申请4695股，即47手<br>
   * 11：”26.91″，“买一”报价<br>
   * 12：”57590″，“买二” <br>
   * 13：”26.90″，“买二” <br>
   * 14：”14700″，“买三” <br>
   * 15：”26.89″，“买三” <br>
   * 16：”14300″，“买四” <br>
   * 17：”26.88″，“买四” <br>
   * 18：”15100″，“买五” <br>
   * 19：”26.87″，“买五” <br>
   * 20：”3100″，“卖一”申报3100股，即31手<br>
   * 21：”26.92″，“卖一”报价 (22, 23), (24, 25), (26,27), (28, 29)分别为“卖二”至“卖四的情况” 30：”2008-01-11″，日期<br>
   * 31：”15:05:32″，时间<br>
   *
   * @throws IOException
   */
  public String queryLatest() throws IOException
  {
    return StringUtils.convertStreamToString(FileUtils.downloadFile("http://hq.sinajs.cn/list=" + getCodeWithAddr()), "gb2312");
  }
  
  /**
   * @return 最近成交价
   *
   * @throws IOException
   */
  public Float getCurrentPrice() throws IOException
  {
    return Float.valueOf(queryLatest().split(",")[3]);
  }
  
  /**
   * @return 当前买1价
   * @throws IOException
   */
  public Float getCurrentMarketBuy() throws IOException
  {
    return Float.valueOf(queryLatest().split(",")[11]);
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
