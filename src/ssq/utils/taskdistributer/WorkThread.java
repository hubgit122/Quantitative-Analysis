package ssq.utils.taskdistributer;

import java.util.Random;

import ssq.stock.Stock;

/**
 * 自定义的工作线程，持有分派给它执行的任务列表
 */
public class WorkThread extends Thread
{
    public static void display()
    {
        System.out.println("test");
    }

    public static void main(String[] args)
    {
        int numOfThreads = 5;
        final int cnt = 552;
        
        TaskList taskList = new TaskList();
        final TaskDistributor distributor = new TaskDistributor(taskList, numOfThreads, WorkThread.class)
        {
            @Override
            public synchronized Task getNext(int lastFinished)
            {
                System.out.println(lastFinished + "进度: " + 100.0 * lastFinished / cnt + "%");
                Stock.test();

                return super.getNext(lastFinished);
            }
            
            @Override
            public void informException(Exception e)
            {
                super.informException(e);
            }
        };
        
        for (int i = 0; i < cnt; i++)
        {
            taskList.add(new Task(i)
            {
                @Override
                public void execute()
                {
                    System.out.println(getTaskId());
                    try
                    {
                        sleep(new Random().nextInt(200));
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            });
        }
        
        distributor.schedule();
        distributor.waitTasksDone();

        for (Task task : taskList)
        {
            if (task.getStatus() != 2)
            {
                System.err.println(task.toString());
            }
        }
    }
    
    //本线程待执行的任务列表，你也可以指为任务索引的起始值
    private TaskDistributor taskDistributor;
    
    public void setTaskDistributor(TaskDistributor taskDistributor)
    {
        this.taskDistributor = taskDistributor;
    }

    private int threadId;
    
    public int getThreadId()
    {
        return threadId;
    }
    
    public void setThreadId(int threadId)
    {
        this.threadId = threadId;
    }

    public WorkThread()
    {
        
    }

    public WorkThread(TaskDistributor taskDistributor, int threadId)
    {
        this.taskDistributor = taskDistributor;
        this.threadId = threadId;
    }
    
    /**
     * 执行被指派的所有任务
     */
    @Override
    public void run()
    {
        for (Task task = taskDistributor.getNext(-1); task != null; task = taskDistributor.getNext(task.getTaskId()))
        {
            task.execute();
        }
    }
}