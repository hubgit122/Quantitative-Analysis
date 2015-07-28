package ssq.utils;

public abstract class NthNode<T>
{
    int          index    = 0;
    NthNode<T>[] children = null;
    T            element;

    protected T getElement()
    {
        return element;
    }
    
    @Override
    public String toString()
    {
        return "node[" + element.toString() + "]";
    }

    protected void setElement(T element)
    {
        this.element = element;
    }
    
    protected int getIndex()
    {
        return index;
    }
    
    protected NthNode<T>[] getChildren()
    {
        return children;
    }
    
    public NthNode(T element)
    {
        setElement(element);
    }

    public NthNode()
    {
        
    }

    public boolean isLeaf()
    {
        return children == null;
    }

    private void alloc()
    {
        if (children == null)
        {
            children = new NthNode[getN()];
        }
    }
    
    public NthNode<T> getChild(int index)
    {
        return children[index];
    }

    public abstract int getN();
    
    public void addChild(NthNode<T> c)
    {
        alloc();
        children[index++] = c;
    }
}
