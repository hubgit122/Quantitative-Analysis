package ssq.stock.analyser;

import java.io.File;
import java.io.IOException;
import java.util.Vector;

import ssq.stock.Stock;
import ssq.utils.FileUtils;
import ssq.utils.LogUtils;

public abstract class Analyzer
{
    public void run(String root) throws Exception
    {
        Vector<File> files = FileUtils.getFilteredListOf(new File(root, "vipdoc/sh/lday/"), true, Stock.shFilter);
        files.addAll(FileUtils.getFilteredListOf(new File(root, "vipdoc/sz/lday/"), true, Stock.szFilter)); //先上海再深圳

        LogUtils.logString("开始分析", "进度信息", false);

        int i = 0;
        
        for (File f : files)
        {
            scan(f);

            if (++i % 100 == 0) //每扫描1000支可能的股票更新显示
            {
                LogUtils.logString("扫描总数: " + i + ", 扫描百分比: " + (100.0 * i / files.size()), "进度信息", false);
            }
        }
    }
    
    abstract void scan(File f) throws IOException;
}
