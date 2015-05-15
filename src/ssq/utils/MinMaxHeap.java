package ssq.utils;

import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class MinMaxHeap<Key> implements Iterable<Key>
{
    private Key[]           pq;        //store items at indices 1 to N
    private int             N;         //number of items on priority queue
    private Comparator<Key> comparator; //optional comparator
                                        
    /**
     * Create an empty priority queue with the given initial capacity.
     */
    public MinMaxHeap(int initCapacity)
    {
        pq = (Key[]) new Object[initCapacity + 1];
        N = 0;
    }
    
    /**
     * Create an empty priority queue.
     */
    public MinMaxHeap()
    {
        this(1);
    }
    
    /**
     * Create an empty priority queue with the given initial capacity, using the given comparator.
     */
    public MinMaxHeap(int initCapacity, Comparator<Key> comparator)
    {
        this.comparator = comparator;
        pq = (Key[]) new Object[initCapacity + 1];
        N = 0;
    }
    
    /**
     * Create an empty priority queue using the given comparator.
     */
    public MinMaxHeap(Comparator<Key> comparator)
    {
        this(1, comparator);
    }
    
    /**
     * Create a priority queue with the given items. Takes time proportional to the number of items using sink-based heap construction.
     */
    public MinMaxHeap(Key[] keys)
    {
        N = keys.length;
        pq = (Key[]) new Object[N + 1];
        for (int i = 0; i < N; i++)
            pq[i + 1] = keys[i];
        for (int k = N / 2; k >= 1; k--)
            sink(k);
        assert isMin_MaxHeap();
    }
    
    /**
     * Is the priority queue empty?
     */
    public boolean isEmpty()
    {
        return N == 0;
    }
    
    /**
     * Return the number of items on the priority queue.
     */
    public int size()
    {
        return N;
    }
    
    /**
     * Return the smallest key on the priority queue. Throw an exception if no such key exists because the priority queue is empty.
     */
    public Key min()
    {
        if (isEmpty())
            throw new RuntimeException("Priority queue underflow");
        return pq[1];
    }
    
    /**
     * Return the largest key on the priority queue. Throw an exception if no such key exists because the priority queue is empty.
     */
    public Key max()
    {
        if (N == 0)
            throw new RuntimeException("Priority queue underflow");
        if (N == 1)
            return pq[1];
        if (N == 2)
            return pq[2];
        return less(1, 2) ? pq[2] : pq[1];
    }
    
    /**
     * Add a new key to the priority queue.
     */
    public void insert(Key x)
    {
        if (N == pq.length - 1)
            resize(2 * pq.length);
        pq[++N] = x;
        swim(N);
        assert isMin_MaxHeap();
    }
    
    /**
     * Delete and return the smallest key on the priority queue. Throw an exception if no such key exists because the priority queue is empty.
     */
    public Key delMin()
    {
        if (N == 0)
            throw new RuntimeException("Priority queue underflow");
        exch(1, N);
        Key min = pq[N--];
        sink(1);
        pq[N + 1] = null;
        if ((N > 0) && (N == (pq.length - 1) / 4))
            resize(pq.length / 2);
        assert isMin_MaxHeap();
        return min;
    }
    
    /**
     * Delete and return the largest key on the priority queue. Throw an exception if no such key exists because the priority queue is empty.
     */
    public Key delMax()
    {
        if (N == 0)
            throw new RuntimeException("Priority queue underflow");
        if (N == 1)
            return delMin();
        if (N == 2)
        {
            Key max = pq[2];
            N--;
            pq[N + 1] = null;
            if ((N > 0) && (N == (pq.length - 1) / 4))
                resize(pq.length / 2);
            return max;
        }
        int m = less(2, 3) ? 3 : 2;
        exch(m, N);
        Key max = pq[N--];
        sink(m);
        pq[N + 1] = null;
        if ((N > 0) && (N == (pq.length - 1) / 4))
            resize(pq.length / 2);
        return max;
    }
    
    //helper function to double the size of the heap array
    private void resize(int capacity)
    {
        Key[] temp = (Key[]) new Object[capacity];
        for (int i = 1; i <= N; i++)
            temp[i] = pq[i];
        pq = temp;
    }
    
    /***********************************************************************
     * Helper functions to restore the heap invariant.
     **********************************************************************/
    private void sink(int k)
    {
        if (isMin(k))
            sinkMin(k);
        else
            sinkMax(k);
    }
    
    private void sinkMin(int k)
    {
        if (2 * k <= N)
        {
            int m;
            if (4 * k > N)
            {
                if (N == 2 * k)
                    m = 2 * k;
                else
                    m = less(2 * k, 2 * k + 1) ? 2 * k : 2 * k + 1;
                if (less(m, k))
                    exch(m, k);
            }
            else
            {
                m = 4 * k;
                for (int i = 4 * k + 1; i <= N && i < 4 * k + 4; i++)
                    if (less(i, m))
                        m = i;
                if (less(m, k))
                {
                    exch(m, k);
                    if (less(m / 2, m))
                        exch(m / 2, m);
                    sinkMin(m);
                }
            }
        }
    }
    
    private void sinkMax(int k)
    {
        if (2 * k <= N)
        {
            int m;
            if (4 * k > N)
            {
                if (N == 2 * k)
                    m = 2 * k;
                else
                    m = less(2 * k, 2 * k + 1) ? 2 * k + 1 : 2 * k;
                if (less(k, m))
                    exch(m, k);
            }
            else
            {
                m = 4 * k;
                for (int i = 4 * k + 1; i <= N && i < 4 * k + 4; i++)
                    if (less(m, i))
                        m = i;
                if (less(k, m))
                {
                    exch(m, k);
                    if (less(m, m / 2))
                        exch(m / 2, m);
                    sinkMax(m);
                }
            }
        }
    }
    
    private void swim(int k)
    {
        if (isMin(k))
        {
            if (k > 1 && less(k / 2, k))
            {
                exch(k, k / 2);
                swimMax(k / 2);
            }
            else
                swimMin(k);
        }
        else
        {
            if (k > 1 && less(k, k / 2))
            {
                exch(k, k / 2);
                swimMin(k / 2);
            }
            else
                swimMax(k);
        }
        
    }
    
    private void swimMin(int k)
    {
        if (k / 4 > 0 && less(k, k / 4))
        {
            exch(k, k / 4);
            swimMin(k / 4);
        }
    }
    
    private void swimMax(int k)
    {
        if (k / 4 > 0 && less(k / 4, k))
        {
            exch(k, k / 4);
            swimMax(k / 4);
        }
    }
    
    private boolean less(int i, int j)
    {
        if (comparator == null)
        {
            return ((Comparable<Key>) pq[i]).compareTo(pq[j]) < 0;
        }
        else
        {
            return comparator.compare(pq[i], pq[j]) < 0;
        }
    }
    
    private void exch(int i, int j)
    {
        Key swap = pq[i];
        pq[i] = pq[j];
        pq[j] = swap;
    }
    
    // is node k is on min level?
    private boolean isMin(int k)
    {
        double r = Math.log(k + 1) / Math.log(2.0);
        int floor = (int) Math.ceil(r);
        return (floor & 1) == 1;
    }
    
    // is pq[1..N] a min-max heap?
    private boolean isMin_MaxHeap()
    {
        return isMin_MaxHeap(1);
    }
    
    // is subtree of pq[1..N] rooted at k a min-max heap?
    private boolean isMin_MaxHeap(int k)
    {
        if (k > N)
            return true;
        int left = 2 * k, right = 2 * k + 1;
        if (isMin(k))
        {
            if (left <= N && less(left, k))
                return false;
            if (right <= N && less(right, k))
                return false;
        }
        else
        {
            if (left <= N && less(k, left))
                return false;
            if (right <= N && less(k, right))
                return false;
        }
        
        return isMin_MaxHeap(left) && isMin_MaxHeap(right);
    }
    
    /***********************************************************************
     * Iterators
     **********************************************************************/
    
    /**
     * Return an iterator that iterates over all of the keys on the priority queue in ascending order.
     * <p>
     * The iterator doesn't implement <tt>remove()</tt> since it's optional.
     */
    @Override
            public Iterator<Key> iterator()
    {
        return new Min_MaxHeapIterator();
    }
    
    private class Min_MaxHeapIterator implements Iterator<Key>
    {
        // create a new pq
        private MinMaxHeap<Key> copy;
        
        // add all items to copy of heap
        // takes linear time since already in heap order so no keys move
        public Min_MaxHeapIterator()
        {
            if (comparator == null)
                copy = new MinMaxHeap<Key>(size());
            else
                copy = new MinMaxHeap<Key>(size(), comparator);
            for (int i = 1; i <= N; i++)
                copy.insert(pq[i]);
        }
        
        @Override
                public boolean hasNext()
        {
            return !copy.isEmpty();
        }
        
        @Override
                public void remove()
        {
            throw new UnsupportedOperationException();
        }
        
        @Override
                public Key next()
        {
            if (!hasNext())
                throw new NoSuchElementException();
            return copy.delMin();
        }
    }
}
