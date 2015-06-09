package ssq.utils.taskdistributer;


/**
 * 自定义的工作线程，持有分派给它执行的任务列表
 */
public class WorkThread extends Thread
{
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