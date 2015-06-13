/*
 * Created on 2004-10-18
 * @author michaell
 */
package ssq.utils.gui.treetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.DefaultTreeSelectionModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreePath;

public class JTreetable extends JTable
{
    protected TreetableCellRenderer treeCell;

    public JTreetable(DefaultTreetableModel treetableModel)
    {
        super();
        //create a tree
        treeCell = new TreetableCellRenderer(treetableModel);

        super.setModel(new TreetableModelAdapter(treeCell));
        
        resetSeleModel();
        
        //Install the tree editor renderer and editor 
        //    	setDefaultRenderer(TreetableModel.class, treeCell);
        //      setDefaultEditor(TreetableModel.class,new TreeTableCellEditor());
        getColumnModel().getColumn(0).setCellRenderer(treeCell);
        getColumnModel().getColumn(0).setCellEditor(new TreeTableCellEditor());

        setShowGrid(true);
        setIntercellSpacing(new Dimension(1, 1));
        
        //Make tree and table row height same
        if (treeCell.getRowHeight() < 1)
        {
            this.setRowHeight(18);
        }
    }
    
    public void setColumnWidth(int COL)
    {
        TableColumn col = getColumnModel().getColumn(COL);
        col.setWidth(0);
    }
    
    public void resetSeleModel()
    {
        //force JTable and JTree to share their row selection model
        treeCell.setSelectionModel(new DefaultTreeSelectionModel()
        {
            {
                setSelectionModel(listSelectionModel);
            }
        });
    }
    
    /**
     * Workaround for BasicTableUI anomaly. Make sure the UI never tries to paint the editor. The UI currently uses different techniques to paint the renderers and editors and overriding setBounds() below is not the right thing to do for an editor. Returning -1 for the editing row in this case, ensures the editor is never painted.
     */
    @Override
    public int getEditingRow()
    {
        return (getColumnClass(editingColumn)) == TreetableModel.class ? -1 : editingRow;
    }
    
    /**
     * Following methods are utility tools.
     */
    public ArrayList getLeafArrayList()
    {
        DefaultMutableTreeNode root = getTreeRoot();
        if (root == null)
            return null;

        ArrayList array = new ArrayList();
        nodeToArrayList(root, array);

        return array;
    }
    
    private void nodeToArrayList(DefaultMutableTreeNode node, ArrayList array)
    {
        if (node.isLeaf())
        {
            Object obj = node.getUserObject();
            if (obj == null)
            {
                array.add(null);
            }
            else if (obj instanceof TreetableUserClass)
            {
                array.add(((TreetableUserClass) obj).toItem(node));
            }
            else
            {
                array.add(obj);
            }
        }

        for (int i = 0; i < node.getChildCount(); i++)
        {
            DefaultMutableTreeNode childi = (DefaultMutableTreeNode) node.getChildAt(i);
            nodeToArrayList(childi, array);
        }
    }
    
    public DefaultMutableTreeNode getTreeNodeForSelectedRow()
    {
        TreePath path = getTree().getPathForRow(getSelectedRow());
        if (path == null)
            return null;
        return (DefaultMutableTreeNode) path.getLastPathComponent();
    }
    
    public DefaultMutableTreeNode getTreeRoot()
    {
        if (getTreeModel().getRoot() == null)
            return null;
        return (DefaultMutableTreeNode) (getTreeModel().getRoot());
    }
    
    public DefaultTreeModel getTreeModel()
    {
        return (DefaultTreeModel) getTree().getModel();
    }
    
    public int getTreeRowForNode(DefaultMutableTreeNode node)
    {
        return getTree().getRowForPath(new TreePath(getTreeModel().getPathToRoot(node)));
    }
    
    public JTree getTree()
    {
        return treeCell;
    }
    
    public void setColumnComboBox(int col, JComboBox comboBox)
    {
        TableColumn column = getColumnModel().getColumn(col);
        column.setCellEditor(new DefaultCellEditor(comboBox));
        //set tooltip for this column
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText("This is a drop-down list. Please click it.");
        column.setCellRenderer(renderer);
    }
    
    public void setColumnCheckBox(int col, JCheckBox checkBox)
    {
        TableColumn column = getColumnModel().getColumn(col);
        column.setCellEditor(new DefaultCellEditor(checkBox));
        //set tooltip for this column
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setToolTipText("This is a checkbox. Please click it.");
        column.setCellRenderer(renderer);
    }
    
    public void setTreeRoot(DefaultMutableTreeNode root)
    {
        getTreeModel().setRoot(root);
    }
    
    @Override
    public void setValueAt(Object obj, int row, int col)
    {
        //        super.setValueAt(obj,row,col);
        ((AbstractTableModel) this.getModel()).setValueAt(obj, row, col);
        ((AbstractTableModel) this.getModel()).fireTableCellUpdated(row, col);
        //        refresh();
    }
    
    /**
     * Set one node or all tree's nodes to the value.
     * 
     * @param node
     *            The node and its all children will be set to the value. If node == null, then set all rows.
     * @param col
     *            The column will be set.
     * @param value
     *            The value to be set to column.
     */
    public void setRowsOfColumnNode(DefaultMutableTreeNode node, int col, Object value)
    {
        if (node == null)
            node = getTreeRoot();
        Object obj = node.getUserObject();
        if (obj instanceof TreetableUserClass)
        {
            ((TreetableUserClass) obj).setValueAt(col, value);
            for (int i = 0; i < node.getChildCount(); i++)
            {
                setRowsOfColumnNode(((DefaultMutableTreeNode) node.getChildAt(i)), col, value);
            }
        }
    }
    
    public void refresh()
    {
        ((TreetableModelAdapter) this.getModel()).delayedFireTableDataChanged();
    }
    
    public void expandAll()
    {
        if (getTreeRoot() == null)
            return;
        expandNode(getTreeRoot());
    }
    
    //expand the selected node's children nodes
    public void expandNode(DefaultMutableTreeNode node)
    {
        if (node.isLeaf())
            return;
        
        TreePath p = new TreePath(getTreeModel().getPathToRoot(node));
        getTree().expandPath(p);

        for (int i = 0; i < node.getChildCount(); i++)
        {
            DefaultMutableTreeNode c = (DefaultMutableTreeNode) node.getChildAt(i);
            expandNode(c);
        }
    }
    
    class TreetableCellRenderer extends JTree implements TableCellRenderer
    {
        protected int visibleRow;

        public TreetableCellRenderer(DefaultTreeModel model)
        {
            super(model);
        }
        
        /**
         * updateUI is overridden to set the colors of the Tree's renderer to match that of the table.
         */
        @Override
        public void updateUI()
        {
            super.updateUI();
            // Make the tree's cell renderer use the table's cell selection colors. 
            TreeCellRenderer tcr = getCellRenderer();
            if (tcr instanceof TreeCellRenderer)
            {
                DefaultTreeCellRenderer dtcr = ((DefaultTreeCellRenderer) tcr);
                // For 1.1 uncomment this, 1.2 has a bug that will cause an
                // exception to be thrown if the border selection color is
                // null.
                //    		dtcr.setBorderSelectionColor(null);
                dtcr.setBackgroundNonSelectionColor(Color.BLUE);
                dtcr.setTextSelectionColor(UIManager.getColor("Table.selectionForeground"));
                dtcr.setBackgroundSelectionColor(UIManager.getColor("Table.selectionBackground"));
            }
        }
        
        /**
         * Sets the row height of the tree, and forwards the row height to the table.
         */
        @Override
        public void setRowHeight(int rowHeight)
        {
            if (rowHeight > 0)
            {
                super.setRowHeight(rowHeight);
                if (JTreetable.this != null && JTreetable.this.getRowHeight() != rowHeight)
                    JTreetable.this.setRowHeight(getRowHeight());
            }
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column)
        {
            if (isSelected)
                setBackground(table.getSelectionBackground());
            //                setBackground(Color.RED);
            else
                setBackground(table.getBackground());

            visibleRow = row;
            return this;
        }
        
        @Override
        public void paint(Graphics g)
        {
            g.translate(0, -visibleRow * getRowHeight());
            super.paint(g);
        }
        
        @Override
        public void setBounds(int x, int y, int w, int h)
        {
            super.setBounds(x, 0, w, JTreetable.this.getHeight());
        }
    }

    class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor
    {
        //        public Object getCellEditorValue(){return treeCell;}
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int r, int c)
        {
            return treeCell;
            //            return new Checkbox();
        }
    }
}
