/*
 * TableRenderDemo.java is a 1.4 application that requires no other files.
 */

package ssq.utils.gui.treetable;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * TableRenderDemo is just like TableDemo, except that it explicitly initializes column sizes and it uses a combo box as an editor for the Sport column.
 */
public class TableRenderDemo extends JPanel
{
    private boolean DEBUG = false;
    
    public TableRenderDemo()
    {
        super(new GridLayout(1, 0));
        
        JTable table = new JTable(new MyTableModel());
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        
        //Create the scroll pane and add the table to it.
        JScrollPane scrollPane = new JScrollPane(table);
        
        //Set up column sizes.
        initColumnSizes(table);
        
        //Fiddle with the Sport column's cell editors/renderers.
        setUpSportColumn(table.getColumnModel().getColumn(2));
        setCheckColumn(table);
        
        //Add the scroll pane to this panel.
        add(scrollPane);
    }
    
    /**
     * These two classes CheckBoxRender and CheckBoxEditor can: 1. Control checkbox color 2. values among multi-rows
     */
    class CheckBoxRender extends DefaultTableCellRenderer
    {
        protected int visibleRow;
        JCheckBox     chBox;
        
        public CheckBoxRender()
        {
            super();
            chBox = new JCheckBox();
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                boolean hasFocus, int row, int column)
        {
            if (isSelected)
            {
                setForeground(table.getSelectionForeground());
            }
            else
            {
                setBackground(table.getBackground());
            }
            if (value instanceof Boolean)
            {
                if (((Boolean) value).booleanValue())
                {
                    chBox.setBackground(Color.BLUE);
                }
                else
                {
                    chBox.setBackground(Color.RED);
                }
                chBox.setSelected(((Boolean) value).booleanValue());
            }
            if (row == 1)
            {
                table.setValueAt(new Boolean(!((Boolean) value).booleanValue()), 2, column);
                table.setValueAt(new Boolean(!((Boolean) value).booleanValue()), 3, column);
            }
            return chBox;
        }
    }
    
    class CheckBoxEditor extends DefaultCellEditor
    {
        public CheckBoxEditor()
        {
            super(new JCheckBox());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column)
        {
            if (value instanceof Boolean)
            {
                if (((Boolean) value).booleanValue())
                {
                    ((JCheckBox) getComponent()).setEnabled(true);
                }
                else
                {
                    ((JCheckBox) getComponent()).setEnabled(false);
                }
            }
            super.getTableCellEditorComponent(table, value, isSelected, row, column);
            System.out.println("row " + row + ", column " + column + ", value = " + value);
            return getComponent();
        }
    }
    
    public void setCheckColumn(JTable table)
    {
        TableColumn sportColumn = table.getColumnModel().getColumn(1);
        
        sportColumn.setCellRenderer(new CheckBoxRender());
        sportColumn.setCellEditor(new CheckBoxEditor());
    }
    
    private JComboBox createComboBox(int i)
    {
        JComboBox combo = new JComboBox();
        combo.addItem(new Integer(i++));
        combo.addItem(new Integer(i++));
        return combo;
    }
    
    class ComboEditor extends DefaultCellEditor
    {
        JComboBox comboBox;
        
        public ComboEditor(JComboBox c)
        {
            super(c);
            System.out.println(" combo editor init ...");
        }
        
        private void recon(int row, JComboBox combox)
        {
            combox.removeAllItems();
            combox.addItem(new Integer(row + 2).toString());
            combox.addItem(new Integer(row + 3).toString());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table,
                Object value, boolean isSelected, int row, int column)
        {
            super.getTableCellEditorComponent(table, value, isSelected, row, column);
            comboBox = (JComboBox) getComponent();
            recon(row, comboBox);
            System.out.println("row " + row + ", column " + column + ", OldValue = " +
                    value + ", SelectValue = " + comboBox.getSelectedItem());
            return comboBox;
        }
    }
    
    public void setUpSportColumn(TableColumn sportColumn)
    {
        //Set up the editor for the sport cells.
        sportColumn.setCellEditor(new ComboEditor(createComboBox(0)));
        sportColumn.setCellRenderer(new DefaultTableCellRenderer());
    }
    
    /*
     * This method picks good column sizes.
     * If all column heads are wider than the column's cells'
     * contents, then you can just use column.sizeWidthToFit().
     */
    private void initColumnSizes(JTable table)
    {
        MyTableModel model = (MyTableModel) table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longValues = model.longValues;
        TableCellRenderer headerRenderer =
                table.getTableHeader().getDefaultRenderer();
        
        for (int i = 0; i < 5; i++)
        {
            column = table.getColumnModel().getColumn(i);
            
            comp = headerRenderer.getTableCellRendererComponent(
                    null, column.getHeaderValue(),
                    false, false, 0, 0);
            headerWidth = comp.getPreferredSize().width;
            
            comp = table.getDefaultRenderer(model.getColumnClass(i)).
                    getTableCellRendererComponent(
                            table, longValues[i],
                            false, false, 0, i);
            cellWidth = comp.getPreferredSize().width;
            
            if (DEBUG)
            {
                System.out.println("Initializing width of column "
                        + i + ". "
                        + "headerWidth = " + headerWidth
                        + "; cellWidth = " + cellWidth);
            }
            
            //XXX: Before Swing 1.1 Beta 2, use setMinWidth instead.
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }
    
    class MyTableModel extends AbstractTableModel
    {
        private String[]      columnNames = { "First Name",
                                                  "Last Name",
                                                  "Sport",
                                                  "# of Years",
                                                  "Vegetarian" };
        private Object[][]    data        = {
                                                  { "Mary", new Boolean(true),
                                                          "0", new Integer(5), new Boolean(false) },
                                                  { "Alison", new Boolean(true),
                                                          "0", new Integer(3), new Boolean(true) },
                                                  { "Kathy", new Boolean(true),
                                                          "0", new Integer(2), new Boolean(false) },
                                                  { "Sharon", new Boolean(true),
                                                          "0", new Integer(20), new Boolean(true) },
                                                  { "Philip", new Boolean(true),
                                                          "0", new Integer(10), new Boolean(false) }
                                          };
        
        public final Object[] longValues  = { "Sharon", new Boolean(true),
                                                  "None of the above",
                                                  new Integer(20), Boolean.TRUE };
        
        @Override
        public int getColumnCount()
        {
            return columnNames.length;
        }
        
        @Override
        public int getRowCount()
        {
            return data.length;
        }
        
        @Override
        public String getColumnName(int col)
        {
            return columnNames[col];
        }
        
        @Override
        public Object getValueAt(int row, int col)
        {
            return data[row][col];
        }
        
        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */
        @Override
        public Class getColumnClass(int c)
        {
            return getValueAt(0, c).getClass();
        }
        
        /*
         * Don't need to implement this method unless your table's
         * editable.
         */
        @Override
        public boolean isCellEditable(int row, int col)
        {
            //Note that the data/cell address is constant,
            //no matter where the cell appears onscreen.
            if (col < 1)
            {
                return false;
            }
            else
            {
                return true;
            }
        }
        
        /*
         * Don't need to implement this method unless your table's
         * data can change.
         */
        @Override
        public void setValueAt(Object value, int row, int col)
        {
            if (DEBUG)
            {
                System.out.println("Setting value at " + row + "," + col
                        + " to " + value
                        + " (an instance of "
                        + value.getClass() + ")");
            }
            
            data[row][col] = value;
            fireTableCellUpdated(row, col);
            
            if (DEBUG)
            {
                System.out.println("New value of data:");
                printDebugData();
            }
        }
        
        private void printDebugData()
        {
            int numRows = getRowCount();
            int numCols = getColumnCount();
            
            for (int i = 0; i < numRows; i++)
            {
                System.out.print("    row " + i + ":");
                for (int j = 0; j < numCols; j++)
                {
                    System.out.print("  " + data[i][j]);
                }
                System.out.println();
            }
            System.out.println("--------------------------");
        }
    }
    
    /**
     * Create the GUI and show it. For thread safety, this method should be invoked from the event-dispatching thread.
     */
    private static void createAndShowGUI()
    {
        //Make sure we have nice window decorations.
        JFrame.setDefaultLookAndFeelDecorated(true);
        
        //Create and set up the window.
        JFrame frame = new JFrame("TableRenderDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //Create and set up the content pane.
        TableRenderDemo newContentPane = new TableRenderDemo();
        newContentPane.setOpaque(true); //content panes must be opaque
        frame.setContentPane(newContentPane);
        
        //Display the window.
        frame.pack();
        frame.setVisible(true);
    }
    
    public static void main(String[] args)
    {
        //Schedule a job for the event-dispatching thread:
        //creating and showing this application's GUI.
        javax.swing.SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                createAndShowGUI();
            }
        });
    }
}
