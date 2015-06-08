package ssq.stock;

import java.util.ArrayList;
import java.util.List;

/**
 * 指派任务列表给线程的分发器
 *
 * @author Unmi QQ: 1125535 Email: fantasia@sina.com MSN: kypfos@msn.com 2008-03-25
 */
public class TaskDistributor
{
    /**
     * 测试方法
     *
     * @param args
     */
    public static void main(String[] args)
    {
        //初始化要执行的任务列表
        List<Task> taskList = new ArrayList();
        for (int i = 0; i < 108; i++)
        {
            taskList.add(new Task(i));
        }
        //设定要启动的工作线程数为 5 个
        int threadCount = 5;
        List[] taskListPerThread = distributeTasks(taskList, threadCount);
        System.out.println("实际要启动的工作线程数：" + taskListPerThread.length);
        for (int i = 0; i < taskListPerThread.length; i++)
        {
            Thread workThread = new WorkThread(taskListPerThread[i], i);
            workThread.start();
        }
    }
    
    /**
     * 把 List 中的任务分配给每个线程，先平均分配，剩于的依次附加给前面的线程 返回的数组有多少个元素（List）就表明将启动多少个工作线程
     *
     * @param taskList
     *            待分派的任务列表
     * @param threadCount
     *            线程数
     * @return 列表的数组，每个元素中存有该线程要执行的任务列表
     */
    public static List[] distributeTasks(List<Task> taskList, int threadCount)
    {
        // 每个线程至少要执行的任务数，假如不为零则表示每个线程都会分配到任务
        int minTaskCount = taskList.size() / threadCount;
        // 平均分配后还剩下的任务数，不为零则还有任务依个附加到前面的线程中
        int remainTaskCount = taskList.size() % threadCount;
        // 实际要启动的线程数,如果工作线程比任务还多
        // 自然只需要启动与任务相同个数的工作线程，一对一的执行
        // 毕竟不打算实现了线程池，所以用不着预先初始化好休眠的线程
        int actualThreadCount = minTaskCount > 0 ? threadCount : remainTaskCount;
        // 要启动的线程数组，以及每个线程要执行的任务列表
        List<Task>[] taskListPerThread = new List[actualThreadCount];
        int taskIndex = 0;
        //平均分配后多余任务，每附加给一个线程后的剩余数，重新声明与 remainTaskCount
        //相同的变量，不然会在执行中改变 remainTaskCount 原有值，产生麻烦
        int remainIndces = remainTaskCount;
        for (int i = 0; i < taskListPerThread.length; i++)
        {
            taskListPerThread[i] = new ArrayList();
            // 如果大于零，线程要分配到基本的任务
            if (minTaskCount > 0)
            {
                for (int j = taskIndex; j < minTaskCount + taskIndex; j++)
                {
                    taskListPerThread[i].add(taskList.get(j));
                }
                taskIndex += minTaskCount;
            }
            // 假如还有剩下的，则补一个到这个线程中
            if (remainIndces > 0)
            {
                taskListPerThread[i].add(taskList.get(taskIndex++));
                remainIndces--;
            }
        }
        // 打印任务的分配情况
        for (int i = 0; i < taskListPerThread.length; i++)
        {
            System.out.println("线程 " + i + " 的任务数：" +
                    taskListPerThread[i].size() + " 区间["
                    + taskListPerThread[i].get(0).getTaskId() + ","
                    + taskListPerThread[i].get(taskListPerThread[i].size() - 1).getTaskId() + "]");
        }
        
        return taskListPerThread;
    }
}

/**
 * 要执行的任务，可在执行时改变它的某个状态或调用它的某个操作 例如任务有三个状态，就绪，运行，完成，默认为就绪态 要进一步完善，可为 Task 加上状态变迁的监听器，因之决定UI的显示
 */
class Task
{
    public static final int READY    = 0;
    public static final int RUNNING  = 1;
    public static final int FINISHED = 2;
    private int             status;
    //声明一个任务的自有业务含义的变量，用于标识任务
    private int             taskId;

    //任务的初始化方法
    public Task(int taskId)
    {
        this.status = READY;
        this.taskId = taskId;
    }

    /**
     * 执行任务
     */
    public void execute()
    {
        // 设置状态为运行中
        setStatus(Task.RUNNING);
        System.out.println("当前线程 ID 是：" + Thread.currentThread().getName()
                + " | 任务 ID 是：" + this.taskId);
        // 附加一个延时
        try
        {
            Thread.sleep(1000);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        // 执行完成，改状态为完成
        setStatus(FINISHED);
    }

    public void setStatus(int status)
    {
        this.status = status;
    }

    public int getTaskId()
    {
        return taskId;
    }
}

/**
 * 自定义的工作线程，持有分派给它执行的任务列表
 */
class WorkThread extends Thread
{
    //本线程待执行的任务列表，你也可以指为任务索引的起始值
    private List<Task> taskList = null;
    private int        threadId;

    /**
     * 构造工作线程，为其指派任务列表，及命名线程 ID
     *
     * @param taskList
     *            欲执行的任务列表
     * @param threadId
     *            线程 ID
     */
    public WorkThread(List<Task> taskList, int threadId)
    {
        this.taskList = taskList;
        this.threadId = threadId;
    }

    /**
     * 执行被指派的所有任务
     */
    @Override
    public void run()
    {
        for (Task task : taskList)
        {
            task.execute();
        }
    }
}
