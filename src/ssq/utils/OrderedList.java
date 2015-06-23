package ssq.utils;

import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.ListIterator;

/**
 * 有序链表 对数组进行插入排序
 *
 * @author stone
 */
public class OrderedList<T> extends LinkedList<T>
{
    private static final long serialVersionUID = -3547203938379210212L;
    
    Comparator<T>             comparator;
    
    /**
     * 未提供比较器的, 构造默认比较器, 可能抛出异常
     *
     * @param c
     */
    public OrderedList(Comparator<T> c)
    {
        comparator = c;
    }
    
    public OrderedList()
    {
        comparator = new Comparator<T>()
        {
            @SuppressWarnings("unchecked")
            @Override
            public int compare(T o1, T o2)
            {
                return ((Comparable<T>) o1).compareTo(o2);
            }
        };
    }
    
    //
    //    public static class Tester
    //    {
    //        @Test
    //        public void testInsert()
    //        {
    //            OrderedList<Double> orderedList = new OrderedList<Double>();
    //            orderedList.add(1.0);
    //            orderedList.add(2.0);
    //            orderedList.add(3.0);
    //            
    //            assert (orderedList.insert(2.5) == 2);
    //            assert (orderedList.insert(3.5) == 4);
    //        }
    //        
    //        @Test
    //        public void testComparator()
    //        {
    //            OrderedList<Double> orderedList = new OrderedList<Double>();
    //            
    //            for (int i = 0; i < 100; i++)
    //            {
    //                orderedList.add(Math.random());
    //            }
    //            
    //            System.out.println(orderedList.size());
    //            System.out.println(orderedList);
    //        }
    //
    //        @Test
    //        public void testAdd()
    //        {
    //            OrderedList<Double> orderedList = new OrderedList<Double>();
    //            
    //            for (int i = 0; i < 100; i++)
    //            {
    //                orderedList.add(Math.random());
    //            }
    //            
    //            System.out.println(orderedList.size());
    //            System.out.println(orderedList);
    //        }
    //    }
    
    public int insert(T e)
    {
        for (ListIterator<T> iterator = this.listIterator(); iterator.hasNext();)
        {
            T current = iterator.next();
            
            if (comparator.compare(e, current) <= 0)
            {
                iterator.previous();
                iterator.add(e);
                return iterator.nextIndex();
            }
        }
        
        super.addLast(e);
        return size() - 1;
    }
    
    /**
     * 有序加入
     *
     * @param e
     * @return
     */
    @Override
    public boolean add(T e)
    {
        insert(e);
        return true;
    }
    
    @Override
    public void addFirst(T e)
    {
        add(e);
    }
    
    @Override
    public T poll()
    {
        // TODO 自动生成的方法存根
        return super.poll();
    }
    
    @Override
    public boolean offer(T e)
    {
        return add(e);
    }
    
    @Override
    public boolean offerFirst(T e)
    {
        return add(e);
    }
    
    @Override
    public boolean offerLast(T e)
    {
        return add(e);
    }
    
    @Override
    public void addLast(T e)
    {
        add(e);
    }
    
    @Override
    public boolean addAll(Collection<? extends T> c)
    {
        for (T t : c)
        {
            add(t);
        }
        return true;
    }
    
    @Override
    public boolean addAll(int index, Collection<? extends T> c)
    {
        for (T t : c)
        {
            add(t);
        }
        return true;
    }
    
    @Override
    public T set(int index, T element)
    {
        remove(index);
        add(element);
        return element;
    }
    
    @Override
    public void add(int index, T element)
    {
        add(element);
    }
    
    @Override
    public void push(T e)
    {
        add(e);
    }
}
