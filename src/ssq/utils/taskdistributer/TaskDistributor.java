package ssq.utils.taskdistributer;

import java.util.LinkedList;

/**
 * 指派任务列表给线程的分发器
 *
 */
public class TaskDistributor
{
    final private TaskList taskList;
    int                    notFinishedIndex = 0; //指向第一个未完成的任务(可能在运行)
    int                    toBeRunIndex     = 0; //指向第一个需要被分配的任务(可能运行失败过并被要求重复运行)
    LinkedList<WorkThread> threads;

    public TaskDistributor(TaskList taskList, int capacity, Class<? extends WorkThread> threadClass)
    {
        this.taskList = taskList;
        this.threads = new LinkedList<>();
        
        for (int i = 0; i < capacity; i++)
        {
            WorkThread tmp;
            try
            {
                tmp = threadClass.newInstance();
            }
            catch (Exception e)
            {
                e.printStackTrace();
                return;
            }
            tmp.setTaskDistributor(this);
            tmp.setThreadId(i);
            threads.add(tmp);
        }
    }
    
    public void schedule()
    {
        for (WorkThread thread : threads)
        {
            thread.start();
        }
    }
    
    public synchronized Task getNext(int lastFinished)
    {
        int size = taskList.size();
        if (lastFinished >= 0)
        {
            taskList.get(lastFinished).setFinished();

            //            if (lastFinished == notFinishedIndex)
            //            {
            //                for (int i = notFinishedIndex + 1; i < size; i++)
            //                {
            //                    if (taskList.get(i).getStatus() != Task.FINISHED)
            //                    {
            //                        notFinishedIndex = i;
            //                    }
            //                }
            //
            //                if (notFinishedIndex == lastFinished)
            //                {
            //                    notFinishedIndex = size; // 结束了
            //                }
            //            }
        }

        for (int i = toBeRunIndex; i < size; i++)
        {
            Task task = taskList.get(i);
            if (task.getStatus() == Task.READY)
            {
                toBeRunIndex = i + 1;
                return task;
            }
        }

        toBeRunIndex = size;
        return null;
    }
    
    public synchronized void redoTask(int taskid)
    {
        assert taskList.get(taskid).getStatus() != Task.READY;
        taskList.get(taskid).setReady();
        toBeRunIndex = Math.min(toBeRunIndex, taskid);
    }
    
    public void informException(Exception e)
    {
    }

    public void waitTasksDone()
    {
        for (WorkThread thread : threads)
        {
            try
            {
                thread.join();
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
    }
}
