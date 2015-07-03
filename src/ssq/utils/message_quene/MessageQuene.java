package ssq.utils.message_quene;

import java.util.concurrent.LinkedBlockingQueue;

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
    private static final long serialVersionUID = 1L;

    String                    name;
    TaskList                  informThreads    = new TaskList();
    TaskDistributor           distributor;
    JSONObject                msg;
    
    public MessageQuene(String name)
    {
        this.name = name;
        distributor = new TaskDistributor(informThreads, 10);
        
        new Thread()
        {
            @Override
            public void run()
            {
                while (true)
                {
                    try
                    {
                        msg = take();
                        distributor.schedule();
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
    
    class MyTask extends Task
    {
        Receiver receiver;

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

    public MessageQuene register(Receiver receiver)
    {
        informThreads.add(new MyTask(informThreads.size(), receiver));
        return this;
    }

    public static void test()
    {
        MessageQuene mq = new MessageQuene("test");

        for (int i = 0; i < 10; i++)
        {
            final int j = i;
            mq.register(new Receiver()
            {
                private static final long serialVersionUID = 1L;

                @Override
                public void consume(JSONObject msg)
                {
                    System.out.println("--------" + j + "------------" + msg.toString());
                }
            });
        }
        
        int i = 0;
        while (true)
        {
            i++;
            try
            {
                Thread.sleep(500);
            }
            catch (InterruptedException e)
            {
            }
            
            mq.offer(JSONObject.fromObject("{\"id\" : " + i + "}"));
        }
    }
}
