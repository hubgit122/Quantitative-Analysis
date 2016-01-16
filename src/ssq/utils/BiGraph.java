package ssq.utils;

public class BiGraph<T> extends NthGraph<T>
{
    public BiGraph(Class<? extends NthNode> c, T[] nodes, Pair<T, T>[] edges)
    {
        super(c, nodes, edges);
    }
    
    public BiGraph(Class<? extends NthNode> c, T[] nodes)
    {
        super(c, nodes);
    }
    
    @Override
    public int getN()
    {
        return 2;
    }
}
