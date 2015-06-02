/*
 * Created on 2004-10-20
 * @author michaell
 */
package ssq.utils.gui.treetable;

import java.util.HashMap;

import javax.swing.tree.DefaultMutableTreeNode;

public abstract class TreetableUserClass
{
    private Class[] colClass  = null;
    private HashMap colValues = null;

    protected TreetableUserClass()
    {
        colValues = new HashMap();
    }
    
    protected TreetableUserClass(Object[] values)
    {
        colValues = new HashMap();
        colClass = new Class[values.length];

        for (int i = 0; i < values.length; i++)
        {
            colValues.put(new Integer(i), values[i]);
            colClass[i] = values[i].getClass();
        }
    }
    
    protected HashMap getValues()
    {
        return colValues;
    }
    
    public void setValueAt(int column, Object value)
    {
        colValues.put(new Integer(column), value);
    }
    
    public Object getValueAt(int column)
    {
        return colValues.get(new Integer(column));
    }
    
    @Override
    public String toString()
    {
        return (String) colValues.get(new Integer(0));
    }

    //  Get column-classes from values
    protected Class[] getColumnClasses()
    {
        if (colClass == null)
        {
            if (colValues == null)
                return colClass;

            colClass = new Class[colValues.size()];
            for (int i = 0; i < colValues.size(); i++)
            {
                colClass[i] = colValues.get(new Integer(i)).getClass();
            }
        }
        return colClass;
    }
    
    // User utility method
    public DefaultMutableTreeNode convertTree(DefaultMutableTreeNode src)
    {
        if (src == null)
            return null;
        DefaultMutableTreeNode tar;
        tar = wrapOneNode(src);
        for (int i = 0; i < src.getChildCount(); i++)
        {
            tar.add(convertTree((DefaultMutableTreeNode) src.getChildAt(i)));
        }
        return tar;
    }
    
    /**
     * This methods should be implements by its inherited class, and use convertTree() to recursive call this method.
     * 
     * @param src
     *            The source node will be changed into this class-object node.
     * @return
     */
    protected DefaultMutableTreeNode wrapOneNode(DefaultMutableTreeNode src)
    {
        return src;
    }
    
    public Object toItem(DefaultMutableTreeNode node)
    {
        String s = "";
        // get all column values
        for (int i = 0; i < colValues.size(); i++)
        {
            Object o = colValues.get(new Integer(i));
            s = s + " " + o.toString();
        }
        return s;
    }
}
