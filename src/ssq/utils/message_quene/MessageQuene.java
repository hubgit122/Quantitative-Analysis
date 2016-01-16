package ssq.utils.message_quene;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import ssq.utils.taskdistributer.Task;
import ssq.utils.taskdistributer.TaskDistributor;
import ssq.utils.taskdistributer.TaskList;

/**
 * 进程间的消息队列. 特点是将单个消费者扩充位多个消费者.
 *
 * @author s
 */
public class MessageQuene extends LinkedBlockingQueue<JSONObject>
{
    private static final long  serialVersionUID = 1L;
    public static final String SOME_RECEIVERS   = "someReceivers";
    
    String                     name;
    TaskList                   informTasks      = new TaskList();
    TaskDistributor            distributor;
    JSONObject                 msg;

    public MessageQuene(String name)
    {
        this.name = name;
        distributor = new TaskDistributor(informTasks, 10);
        
        startDispatch();
    }

    private void startDispatch()
    {
        // 一个不断读取消息并分发的线程
        new Thread()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        msg = take(); //阻塞
                        
                        try
                        { //尝试点对点
                            final JSONArray dst = msg.getJSONArray(SOME_RECEIVERS);
                            
                            int size = dst.size();
                            for (int i = 0; i < size; i++)
                            {
                                final int j = i;
                                new Thread()
                                {
                                    @Override
                                    public void run()
                                    {
                                        synchronized (informTasks)
                                        {
                                            informTasks.get(dst.getInt(j)).execute();
                                        }
                                    }
                                }.start();
                            }
                        }
                        catch (Exception e)
                        { //广播
                            distributor.schedule();
                        }
                    }
                    catch (InterruptedException e)
                    {
                        try
                        {
                            Thread.sleep(100);
                        }
                        catch (InterruptedException e1)
                        {
                        }
                    }
                }
            }
        }.start();
    }
    
    class MyTask extends Task implements Serializable
    {
        private static final long serialVersionUID = 1L;
        Receiver                  receiver;
        
        public MyTask(int id, Receiver receiver)
        {
            super(id);

            this.receiver = receiver;
        }
        
        @Override
        public void execute()
        {
            receiver.consume(msg);
        }
    }

    /**
     * 注册一个接收方
     *
     * @return 返回接收方的编号, 用于点对点通信
     */
    public int register(Receiver receiver)
    {
        synchronized (informTasks)
        {
            int number = informTasks.size();
            informTasks.add(new MyTask(number, receiver));

            return number;
        }
    }
    
    public void unregister(int number)
    {
        synchronized (informTasks)
        {
            informTasks.remove(number);
        }
    }
    
    //    public static void test()
    //    {
    //        MessageQuene mq = new MessageQuene("test");
    //
    //        for (int i = 0; i < 10; i++)
    //        {
    //            final int j = i;
    //            mq.register(new Receiver()
    //            {
    //                private static final long serialVersionUID = 1L;
    //
    //                @Override
    //                public void consume(JSONObject msg)
    //                {
    //                    System.out.println("--------" + j + "------------" + msg.toString());
    //                }
    //            });
    //        }
    //
    //        int i = 0;
    //        while (true)
    //        {
    //            i++;
    //            try
    //            {
    //                Thread.sleep(500);
    //            }
    //            catch (InterruptedException e)
    //            {
    //            }
    //
    //            mq.offer(JSONObject.fromObject("{\"id\" : " + i + "}"));
    //        }
    //    }
}
