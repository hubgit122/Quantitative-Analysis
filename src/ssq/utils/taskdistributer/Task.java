package ssq.utils.taskdistributer;

public class Task
{
    public static final int READY    = 0;
    public static final int RUNNING  = 1;
    public static final int FINISHED = 2;
    public static final int ABROTED  = 6;

    final private int       taskId;
    private int             status;
    
    public Task(int taskId)
    {
        this.taskId = taskId;
        status = READY;
    }
    
    /**
     * 多次调用时应该将状态调整为READY
     */
    public void resetStatus()
    {
        setReady();
    }
    
    public void execute()
    {
        System.out.println("当前线程 ID 是：" + Thread.currentThread().getName() + " | 任务 ID 是：" + this.taskId);
    }
    
    public int getTaskId()
    {
        return taskId;
    }
    
    public int getStatus()
    {
        return status;
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    /**
     * 在WorkThread执行遇到问题并决定放弃时, 调用这个方法. 继承这个方法以安全地结束任务(如释放资源等)
     */
    public void onAborted()
    {
        setAborted();
    }
    
    private void setAborted()
    {
        status = ABROTED;
    }
    
    /**
     * 在WorkThread执行遇到问题并决定重启时, 调用这个方法. 继承这个方法以安全地结束并重新初始化任务(如释放并重新分配资源, 恢复各种状态等)
     */
    public void onRedoLater()
    {
        setReady();
    }
    
    public void onStart()
    {
        setRunning();
    }
    
    public void onFinished()
    {
        setFinished();
    }

    private void setFinished()
    {
        status = FINISHED;
    }
    
    private void setReady()
    {
        status = READY;
    }
    
    private void setRunning()
    {
        status = RUNNING;
    }
}
