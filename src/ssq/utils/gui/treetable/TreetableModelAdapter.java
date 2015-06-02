package ssq.utils.gui.treetable;

import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreePath;

/*
 * Created on 2004-10-18
 * @author michaell
 */

class TreetableModelAdapter extends AbstractTableModel
{
    JTree                 treeCell;
    DefaultTreetableModel treetableModel;

    public TreetableModelAdapter(JTree treeCell)
    {
        this.treeCell = treeCell;
        this.treetableModel = (DefaultTreetableModel) treeCell.getModel();
        /** when tree expanding and collapsing,notify table to change */
        treeCell.addTreeExpansionListener(new TreeExpansionListener()
        {
            @Override
            public void treeExpanded(TreeExpansionEvent e)
            {
                fireTableDataChanged();
            }
            
            @Override
            public void treeCollapsed(TreeExpansionEvent e)
            {
                fireTableDataChanged();
            }
        });
        /** When TreeModel changing, notify table to change. */
        treetableModel.addTreeModelListener(new TreeModelListener()
        {
            @Override
            public void treeNodesInserted(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }
            
            @Override
            public void treeStructureChanged(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }
            
            @Override
            public void treeNodesRemoved(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }
            
            @Override
            public void treeNodesChanged(TreeModelEvent e)
            {
                delayedFireTableDataChanged();
            }
        });
    }
    
    public void delayedFireTableDataChanged()
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                fireTableDataChanged();
            }
        });
    }
    
    //local method
    protected Object nodeForRow(int row)
    {
        TreePath treePath = treeCell.getPathForRow(row);
        return treePath.getLastPathComponent();
    }
    
    //AbstractTableModel interfaces
    @Override
    public Object getValueAt(int row, int column)
    {
        return treetableModel.getValueAt(nodeForRow(row), column);
    }
    
    @Override
    public int getRowCount()
    {
        return treeCell.getRowCount();
    }
    
    @Override
    public int getColumnCount()
    {
        return treetableModel.getColumnCount();
    }
    
    @Override
    public String getColumnName(int column)
    {
        return treetableModel.getColumnName(column);
    }
    
    @Override
    public Class getColumnClass(int column)
    { //for show CheckBox
        return treetableModel.getColumnClass(column);
    }
    
    @Override
    public boolean isCellEditable(int row, int column)
    { //for show ComboBox
        return treetableModel.isCellEditable(nodeForRow(row), column);
    }
    
    @Override
    public void setValueAt(Object value, int row, int column)
    {
        treetableModel.setValueAt(value, nodeForRow(row), row, column);
        //        super.fireTableCellUpdated(row, column);
    }
}
