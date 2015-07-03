package ssq.stock.simulator;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import ssq.utils.message_quene.MessageQuene;
import ssq.utils.message_quene.Receiver;

/**
 * 模拟器基类. 管理账户, 负责提供价格信息, 接收买卖委托, 并在合适的时候将委托变为成交
 *
 * @author s
 *
 */
abstract public class Simulator implements Serializable, Receiver
{
    private static final long        serialVersionUID                = 1L;
    public static SimpleDateFormat   dateFormat                      = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat   timeFormat                      = new SimpleDateFormat("HH:mm:ss");
    public static final String       simDir                          = DirUtils.getXxRoot("assets/simulators/");

    /**
     * 模拟器名称, 必须是合法文件名
     */
    String                           name;
    /**
     * 模拟器上的账户列表
     */
    LinkedList<Account>              accounts                        = new LinkedList<>();
    
    /**
     * 接收数据请求
     */
    protected MessageQuene           incomingQueriesQuene;

    /**
     * 发出更新信息
     */
    transient protected MessageQuene outgoingMessagesQuene;
    
    /**
     * 重新读取交易记录的时间步长, 毫秒<br>
     * 这跟模拟器模拟的倍率是两码事. 因为我们没有实时数据接口, 采用轮询的方式, 所以一定要有时间步长.
     */
    int                              timeStep                        = 1000;

    /**
     * 股票代码到接收器的即时成交信息映射列表. "代码->接收器数组"的数组
     */
    JSONObject                       codeToReceiverMapForInstantDeal = new JSONObject();
    
    /**
     * 给新建的模拟器命一个独立的名字. 必须是合法文件名, 否则无法保存
     */
    public Simulator(String name) throws Exception
    {
        if (!FileUtils.isValidFileName(name) || new File(simDir, name).exists())
        {
            throw new Exception("名字里有非法字符");
        }
        
        this.name = name;
        incomingQueriesQuene = new MessageQuene(name + "@incomingQueriesQuene");
        incomingQueriesQuene.register(this);
        outgoingMessagesQuene = new MessageQuene(name + "@outgoingMessagesQuene");
    }
    
    /**
     * 保存模拟器的全部信息到磁盘
     */
    public abstract void save();
    
    /**
     * 从磁盘装载<br>
     * 考虑到接收器的多样性, 不序列化出消息队列了. 反序列化时要重新注册接收器
     */
    public static Simulator load(String name) throws IOException
    {
        ObjectInputStream oi = new ObjectInputStream(new BufferedInputStream(new FileInputStream(new File(simDir, name))));
        Simulator result = null;
        try
        {
            result = (Simulator) oi.readObject();
        }
        catch (ClassNotFoundException e)
        {
        }
        oi.close();

        return result;
    }

    /**
     * 作为接受者, 处理请求
     */
    @Override
    public void consume(JSONObject msg)
    {
        if (msg.getString("type").equals("reregister"))
        {
            int receiverId = msg.getInt("id");
            try
            {
                codeToReceiverMapForInstantDeal.getJSONArray(String.valueOf(msg.getInt("old"))).remove(receiverId);
            }
            catch (Exception e)
            {
            }
            
            codeToReceiverMapForInstantDeal.accumulate(String.valueOf(msg.getInt("new")), receiverId);
        }
    }
    
    /**
     * 设置时间片的跨度
     */
    public void setTimeStep(int step)
    {
        timeStep = step;
    }

    /**
     * 在一个新的时间片, 处理现有的更新请求
     */
    protected void onNewTime()
    {
        informLatest();
        turnMetCommissionsToDeals();
    }
    
    /**
     * 如果最近成交价低于买入委托, 或者高于卖出委托, 则认为在实战中可以成交, 在模拟器里也就成交了
     */
    private void turnMetCommissionsToDeals()
    {
        for (Account account : accounts)
        {
            for (Commission commission : account.commissionList)
            {
                try
                {
                    float commitPrice = commission.price;
                    Float currentPrice = getCurrentPrice(commission.stock.getCode());

                    if (commitPrice != currentPrice && (commission.buy ^ commitPrice < currentPrice))
                    {
                        account.commissionMet(commission, getCurrentTime()); // TODO  如何加入通知比较合适?
                    }
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
    
    public abstract Date getCurrentTime();

    /**
     * 通知最新数据
     */
    private void informLatest()
    {
        for (Object entryo : codeToReceiverMapForInstantDeal.entrySet())
        {
            String entry = (String) entryo;
            
            int code = Integer.valueOf(entry);
            JSONArray receivers = codeToReceiverMapForInstantDeal.getJSONArray(entry);
            String latest = queryLatest(code);
            
            JSONObject result = new JSONObject();
            result.put(MessageQuene.SOME_RECEIVERS, receivers);
            result.put("latest", latest);

            outgoingMessagesQuene.offer(result);
        }
    }

    /**
     * 要求和"http://hq.sinajs.cn/list=sh600525"返回结果的引号里的格式相同, 同时在最前方加入yyyy-MM-dd HH:mm:ss时间域
     */
    public abstract String queryLatest(int code);
    
    /**
     * @return 最近成交价
     */
    public abstract Float getCurrentPrice(int code) throws IOException;
    
    /**
     * 委托买卖
     *
     * @param code
     * @param price
     * @param buy
     * @param quantity
     * @return
     */
    public abstract boolean commit(Account account, int code, float price, boolean buy, int quantity);

    /**
     * 尝试从账户列表找到该名字的账户
     */
    public Account getAccount(String name)
    {
        for (Account account : accounts)
        {
            if (account.name.equals(name))
            {
                return account;
            }
        }
        return null;
    }
    
    /**
     * 没用同名账户, 还是合法文件名, 则可以添加这个账户
     *
     * @param name
     * @param iniVal
     * @return
     */
    public boolean addAccount(String name, float iniVal)
    {
        if (getAccount(name) == null && FileUtils.isValidFileName(name))
        {
            accounts.add(new Account(name, iniVal));
            return true;
        }
        return false;
    }
    
    /**
     * 交易编号
     */
    public int number = 0;

    /**
     * 为新的交易分配编号
     *
     * @return
     */
    public int getNumber()
    {
        return number++;
    }
    
    public void putMessage(JSONObject jsonObject)
    {
        incomingQueriesQuene.offer(jsonObject);
    }
    
    public int registerReceiver(Receiver receiver)
    {
        return outgoingMessagesQuene.register(receiver);
    }

    public void unregister(int number)
    {
        outgoingMessagesQuene.unregister(number);
    }

    /**
     * @param f
     *            交易总金额
     * @return 佣金
     */
    protected static float getCommissionFee(float f)
    {
        return Math.max(f * 0.0003f, 5f);
    }
    
    /**
     *
     * @param f
     *            交易总金额
     * @return 印花税
     */
    protected static float getStampTax(float f)
    {
        return f / 1000f;
    }
}
