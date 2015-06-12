package ssq.stock.analyser;

import java.io.IOException;

import ssq.stock.Stock;
import ssq.stock.gui.GUI;
import ssq.utils.LogUtils;
import ssq.utils.Pair;
import ssq.utils.taskdistributer.Task;
import ssq.utils.taskdistributer.TaskDistributor;
import ssq.utils.taskdistributer.TaskList;
import ssq.utils.taskdistributer.WorkThread;

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

        TaskList taskList = new TaskList();
        TaskDistributor distributor = new TaskDistributor(taskList, 10, WorkThread.class)
        {
            @Override
            public Task getNext(int lastFinished)
            {
                Task result = super.getNext(lastFinished);
                GUI.statusText(getProgressString());
                LogUtils.logString(getProgressString(), "进度信息", false);
                return result;
            }
        };

        int i = 0;

        for (Pair<Integer, String> pair : Stock.stockList)
        {
            final Stock stock = Stock.loadStock(pair.getKey());
            
            if (!stock.getNumberString().matches(filter))
            {
                continue;
            }
            
            taskList.add(new Task(i++)
            {
                @Override
                public void execute()
                {
                    try
                    {
                        scan(stock);
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        
        distributor.schedule();
        distributor.waitTasksDone();
        
        GUI.statusText("扫描结束");
        LogUtils.logString("扫描结束", "进度信息", false);
    }

    /**
     * 要有多线程安全
     *
     * @param stock
     * @throws IOException
     */
    abstract public void scan(Stock stock) throws IOException;
}
