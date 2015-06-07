package ssq.stock;

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
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

import ssq.stock.gui.GUI;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import ssq.utils.StringUtils;

public class Stock implements Serializable
{
    private static final long  serialVersionUID = 3937169104043760931L;
    public static StockList    stockList        = new StockList();
    public static final String stockListPath    = DirUtils.getXxRoot("assets/stockHistories");
    public boolean             isShangHai;
    public int                 number;
    public StockHistory        history;
    transient public String    name;

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
            File out = new File(stockListPath);
            if (!out.exists())
            {
                out.createNewFile();
            }
            
            readList();
            if (JOptionPane.showConfirmDialog(null, "更新数据会花费大量的时间, 请保持网络畅通并关注状态条上的进度提示. ", "更新股票数据? ", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION)
            {
                refreshList();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public static final String filter = "600.*|601.*|603.*|000.*|001.*|002.*|300.*";

    public static void refreshList() throws Exception
    {
        try
        {
            GUI.statusText("开始更新股票信息");
            int cnt = 0;
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
            
            mainloop: for (int place = 0; place < 2; place++)
            {
                Matcher m = place == 1 ? p.matcher(tmp.substring(0, offset)) : p.matcher(tmp.substring(offset));
                while (m.find())
                {
                    String num = m.group(2);

                    if (num.matches(filter))
                    {
                        cnt++;
                        
                        if (cnt < 2408)
                        {
                            continue;
                        }
                        
                        String name = m.group(1);
                        Integer id = Integer.valueOf(num);

                        int insertIndex = stockList.findInsertIndex(id);

                        Stock thisStock = null;
                        while (true)
                        {
                            try
                            {
                                if (insertIndex < 0)
                                {
                                    thisStock = stockList.get(-insertIndex - 1);
                                    thisStock.update(name);
                                }
                                else
                                {
                                    thisStock = new Stock(name, place == 1, id);
                                    stockList.add(insertIndex, thisStock);
                                }
                                break;
                            }
                            catch (Exception e)
                            {
                                if (JOptionPane.showConfirmDialog(null, "请确保网络畅通后点击YES重试. ", "貌似断网了", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE) == JOptionPane.NO_OPTION)
                                {
                                    GUI.statusText("更新部分完成. 更新完成的股票数: " + cnt);
                                    break mainloop;
                                }
                            }
                        }
                        
                        GUI.statusText("更新完成的股票数: " + cnt);
                        saveStock(thisStock);
                    }
                }
            }
            
            GUI.statusText("股票信息更新完毕");
        }
        catch (Exception e)
        {
            if (JOptionPane.showConfirmDialog(null, "如果需要联网更新股票名称并更新股票数据, 请先恢复网络再点击NO. ", "更新股票历史数据时网络无法连接, 使用脱机数据? ", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.NO_OPTION)
            {
                refreshList();
            }
        }
        finally
        {
            saveStockList();
        }
    }
    
    private static void saveStock(Stock stock) throws IOException, FileNotFoundException
    {
        ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(FileUtils.assertFileExists(new File(stockListPath, String.valueOf(pad(stock.number)))))));
        o.writeObject(stock);
        o.close();
    }

    public static void saveStockList()
    {
        try
        {
            for (Stock stock : stockList)
            {
                saveStock(stock);
            }
        }
        catch (IOException e)
        {
            // TODO 自动生成的 catch 块
            e.printStackTrace();
        }
    }
    
    private void update(String name) throws IOException
    {
        this.name = name;
        history.updateData(this);
    }

    public static void readList()
    {
        ArrayList<File> tmp = FileUtils.getFilteredListOf(new File(stockListPath), true, filter);
        
        for (File file : tmp)
        {
            ObjectInputStream i = null;
            try
            {
                i = new ObjectInputStream(new BufferedInputStream(new FileInputStream(file)));
                stockList.add((Stock) i.readObject());
            }
            catch (Exception e1)
            {
                e1.printStackTrace();
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
        }
    }
}
