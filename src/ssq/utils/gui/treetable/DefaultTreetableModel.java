package ssq.utils.gui.treetable;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

/*
 * Created on 2004-10-18
 * @author michaell
 */
public class DefaultTreetableModel extends DefaultTreeModel implements TreetableModel
{
    // Names of the columns.
    private String[] m_cNames = { " At least ", " Two columns " };
    private Class[]  m_cTypes = { TreetableModel.class, String.class };
    
    //constructor: create the root node to show
    public DefaultTreetableModel(String[] columnNames, DefaultMutableTreeNode root)
    {
        super(root);
        if (columnNames != null)
            m_cNames = columnNames;
        verifyColNamesNTypes();
    }
    
    public DefaultTreetableModel(DefaultMutableTreeNode root)
    {
        super(root);
        verifyColNamesNTypes();
    }
    
    public DefaultTreetableModel(String[] columnNames)
    {
        super(null);
        if (columnNames != null)
            m_cNames = columnNames;
        verifyColNamesNTypes();
    }
    
    public DefaultTreetableModel()
    {
        super(null);
    }
    
    /**
     * local method: Get column types from root's AbstractTreetableUserObject Verify types and names
     */
    private void verifyColNamesNTypes()
    {
        if (getRoot() != null)
        {
            Object obj = ((DefaultMutableTreeNode) getRoot()).getUserObject();
            //If user-object class designed ok, and number of column-classes is equaled with column-names
            //and the number > 2
            if (obj != null && obj instanceof TreetableUserClass)
            {
                m_cTypes = ((TreetableUserClass) obj).getColumnClasses();
                if (m_cTypes != null && m_cTypes.length == m_cNames.length && m_cTypes.length > 1)
                {
                    m_cTypes[0] = TreetableModel.class;
                    return;
                }
            }
        }
        if (m_cNames == null || m_cNames.length == 0)
        {
            m_cNames = new String[2];
            m_cNames[0] = " At least ";
            m_cNames[1] = " Two columns ";
            m_cTypes = new Class[2];
        }
        else if (m_cNames.length == 1)
        {
            String s = m_cNames[0];
            m_cNames = new String[2];
            m_cNames[0] = s;
            m_cNames[1] = " Col Name ";
            m_cTypes = new Class[2];
        }
        else
        {
            m_cTypes = new Class[m_cNames.length];
            m_cTypes[0] = TreetableModel.class;
            for (int i = 1; i < m_cNames.length; i++)
            {
                m_cTypes[i] = String.class;
            }
        }
        m_cTypes[0] = TreetableModel.class;
        m_cTypes[1] = String.class;
    }
    
    //    private TreetableUserClass getTreetableUserObject(DefaultMutableTreeNode node){
    //        Object obj = node.getUserObject();
    //        if(obj==null)return null;
    //        return (TreetableUserClass)obj;
    //  	}
    private Object[] getChildRen(Object node)
    {
        DefaultMutableTreeNode treeNode = ((DefaultMutableTreeNode) node);
        int n = treeNode.getChildCount();
        DefaultMutableTreeNode[] child = new DefaultMutableTreeNode[n];
        for (int i = 0; i < n; i++)
        {
            child[i] = (DefaultMutableTreeNode) treeNode.getChildAt(i);
        }
        return child;
    }
    
    //TreeModel interfaces
    @Override
    public Object getChild(Object node, int i)
    {
        return getChildRen(node)[i];
    }
    
    @Override
    public int getChildCount(Object node)
    {
        Object[] children = getChildRen(node);
        return (children == null) ? 0 : children.length;
    }
    
    // TreeTable interface
    @Override
    public String getColumnName(int column)
    {
        verifyColNamesNTypes();
        return m_cNames[column];
    }
    
    @Override
    public Class getColumnClass(int column)
    {
        verifyColNamesNTypes();
        return m_cTypes[column];
    }
    
    @Override
    public int getColumnCount()
    {
        verifyColNamesNTypes();
        if (m_cNames == null)
            return 0;
        return m_cNames.length;
    }
    
    @Override
    public void setValueAt(Object value, Object node, int row, int column)
    {
        DefaultMutableTreeNode anode = ((DefaultMutableTreeNode) node);
        Object obj = anode.getUserObject();
        if (obj instanceof TreetableUserClass)
            ((TreetableUserClass) obj).setValueAt(column, value);
    }
    
    @Override
    public Object getValueAt(Object node, int column)
    {
        DefaultMutableTreeNode anode = ((DefaultMutableTreeNode) node);
        Object obj = anode.getUserObject();
        try
        {
            if (obj instanceof TreetableUserClass)
                return ((TreetableUserClass) obj).getValueAt(column);
        }
        catch (SecurityException se)
        {
        }

        return null;
    }
    
    //implement inherit method TreeTableModel.isCellEditable
    @Override
    public boolean isCellEditable(Object node, int column)
    {
        return true;
    }
}
