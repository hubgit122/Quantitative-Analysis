package ssq.stock;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import ssq.utils.Pair;

public abstract class TableFrame extends JFrame
{
    private static final long serialVersionUID = 3146763463910064509L;
    JTable                    table;
    JLabel                    statusLabel      = new JLabel("列表视图");
    Font                      yahei            = new Font("YaHei", Font.PLAIN, 18);
    File                      file;                                                //数据文件
    JScrollPane               statusPane;
    
    public void setStatusText(String s)
    {
        statusLabel.setText(s);
    }

    public TableFrame()
    {
        this(null);
    }

    public TableFrame(File file)
    {
        this.file = file;
        initTable();
        initView();
        initListeners();
        show();
    }

    private int getPreferredWidthForColumn(TableColumn col)
    {
        int hw = columnHeaderWidth(col); // hw = header width
        int cw = widestCellInColumn(col); // cw = column width
        
        return hw > cw ? hw : cw;
    }
    
    private int columnHeaderWidth(TableColumn col)
    {
        TableCellRenderer renderer = table.getTableHeader().getDefaultRenderer();
        Component comp = renderer.getTableCellRendererComponent(table, col.getHeaderValue(), false, false, 0, 0);
        
        return comp.getPreferredSize().width;
    }
    
    private int widestCellInColumn(TableColumn col)
    {
        int c = col.getModelIndex();
        int width = 0, maxw = 0;
        
        for (int r = 0; r < table.getRowCount(); r++)
        {
            TableCellRenderer renderer = table.getCellRenderer(r, c);
            Component comp = renderer.getTableCellRendererComponent(table, table.getValueAt(r, c), false, false, r, c);
            width = comp.getPreferredSize().width;
            maxw = width > maxw ? width : maxw;
        }
        return maxw;
    }

    protected void initListeners()
    {
        table.addMouseListener(getTableMouseListener());
    }

    abstract protected MouseListener getTableMouseListener();

    private void initView()
    {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        add(new JScrollPane(table), BorderLayout.CENTER);
        table.setFont(yahei);
        TableColumn column = table.getColumnModel().getColumn(0);
        column.setPreferredWidth(getPreferredWidthForColumn(column));
        column = table.getColumnModel().getColumn(1);
        column.setPreferredWidth(getPreferredWidthForColumn(column));
        statusLabel.setFont(yahei);
        pack();
        statusPane = new JScrollPane(statusLabel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
        statusPane.setHorizontalScrollBar(new JScrollBar(Adjustable.HORIZONTAL));
        add(statusPane, BorderLayout.SOUTH);
        setResizable(false);
    }

    public void initTable()
    {
        try
        {
            Pair<Object[][], Object[]> data = toTable();

            DefaultTableModel t = new DefaultTableModel(data.getKey(), data.getValue())
            {
                private static final long serialVersionUID = 1L;
                
                @Override
                public boolean isCellEditable(int row, int column)
                {
                    return false;
                }
            };
            
            table = new JTable(t);
        }

        catch (Exception e1)
        {
            GUI.statusText(e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }
    
    public abstract Pair<Object[][], Object[]> toTable() throws FileNotFoundException, IOException;
}
