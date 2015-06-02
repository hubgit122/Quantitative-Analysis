/*
 * Created on 2004-10-18
 * @author michaell
 */
package ssq.utils.gui.treetable;

import javax.swing.tree.TreeModel;

public interface TreetableModel extends TreeModel
{
    public Object getValueAt(Object node, int column);
    
    public int getColumnCount();
    
    public Class getColumnClass(int column);
    
    public String getColumnName(int column);
    
    public boolean isCellEditable(Object node, int column);
    
    public void setValueAt(Object value, Object node, int row, int column);
}
