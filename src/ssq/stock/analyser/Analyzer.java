package ssq.stock.analyser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import ssq.stock.Stock;
import ssq.stock.gui.GUI;
import ssq.utils.FileUtils;
import ssq.utils.LogUtils;

public abstract class Analyzer
{
    String root;
    String filter = Stock.stockFilter;
    
    public Analyzer(String root, String filter)
    {
        this.root = root;
        this.filter = filter;
    }

    public void run() throws Exception
    {
        filter = filter != null ? filter : Stock.stockFilter;
        
        ArrayList<File> files = FileUtils.getFilteredListOf(new File(root, "vipdoc/sh/lday/"), true, filter);
        files.addAll(FileUtils.getFilteredListOf(new File(root, "vipdoc/sz/lday/"), true, filter)); //先上海再深圳

        GUI.statusText("开始分析");
        LogUtils.logString("开始分析", "进度信息", false);
        
        int i = 0;

        for (File f : files)
        {
            scan(f);
            
            if (++i % 100 == 0) //每扫描1000支可能的股票更新显示
            {
                GUI.statusText("扫描总数: " + i + ", 扫描百分比: " + (100.0 * i / files.size()));
                LogUtils.logString("扫描总数: " + i + ", 扫描百分比: " + (100.0 * i / files.size()), "进度信息", false);
            }
        }
    }
    
    abstract public void scan(File f) throws IOException;
}
