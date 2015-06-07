package ssq.stock.analyser;

import java.io.IOException;

import ssq.stock.Stock;
import ssq.stock.gui.GUI;
import ssq.utils.LogUtils;

public abstract class Analyzer
{
    public final String filter;

    public Analyzer()
    {
        filter = Stock.filter;
    }

    public Analyzer(String filter)
    {
        this.filter = filter;
    }

    public void run() throws Exception
    {
        GUI.statusText("开始分析");
        LogUtils.logString("开始分析", "进度信息", false);
        
        int i = 0;

        for (Stock stock : Stock.stockList)
        {
            if (!String.valueOf(stock.number).matches(filter))
            {
                continue;
            }
            scan(stock);
            
            if (++i % 100 == 0) //每扫描1000支可能的股票更新显示
            {
                GUI.statusText("扫描总数: " + i + ", 扫描百分比: " + (100.0 * i / Stock.stockList.size()));
                LogUtils.logString("扫描总数: " + i + ", 扫描百分比: " + (100.0 * i / Stock.stockList.size()), "进度信息", false);
            }
        }
    }
    
    abstract public void scan(Stock stock) throws IOException;
}
