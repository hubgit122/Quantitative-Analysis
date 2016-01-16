package ssq.utils;

import java.lang.reflect.InvocationTargetException;

/**
 * 所有状态都仅有两个后继状态的状态机
 *
 * @author ssqstone
 */
public class StatusGraph<T extends Enum> extends BiGraph<T>
{
    final Class<? extends Enum> eunmClass;
    BiNode<T>                   currentStatus;

    public BiNode<T> getCurrentStatus()
    {
        return currentStatus;
    }
    
    public T success()
    {
        currentStatus = currentStatus.getChild(0);
        return currentStatus.getElement();
    }
    
    public T fail()
    {
        currentStatus = currentStatus.getChild(1);
        return currentStatus.getElement();
    }
    
    public static StatusGraph getStatusGraph(Class<? extends Enum> c)
    {
        try
        {
            return new StatusGraph(c);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    public StatusGraph(Class<? extends Enum> c) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException
    {
        super(BiNode.class, (T[]) c.getMethod("values").invoke(null, null));
        
        eunmClass = c;
        currentStatus = (BiNode<T>) nodes[0];
    }
}
