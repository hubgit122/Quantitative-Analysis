package ssq.utils;

public class BiNode<T> extends NthNode<T>
{
    public BiNode()
    {
        super();
    }

    public BiNode(T element)
    {
        super(element);
    }

    @Override
    public int getN()
    {
        return 2;
    }
    
    @Override
    public BiNode<T> getChild(int index)
    {
        return (BiNode<T>) super.getChild(index);
    }
    
    @Override
    protected BiNode<T>[] getChildren()
    {
        return (BiNode<T>[]) super.getChildren();
    }
}
