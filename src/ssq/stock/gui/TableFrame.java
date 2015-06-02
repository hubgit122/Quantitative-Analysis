package ssq.stock.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import ssq.utils.Pair;

public abstract class TableFrame extends FrameWithStatus
{
    protected JTable table;
    
    public TableFrame()
    {
        this(null);
    }
    
    /**
     * 需要初始化数据的超类在构造时把初始化数据的InputStream传入本抽象基类的构造函数, 在超类的toTable方法里按需调用
     *
     * @param is
     */
    public TableFrame(InputStream is)
    {
        super(is);
    }

    @Override
    protected void initData()
    {
        updateTable();
    }

    @Override
    protected void initListeners()
    {
        table.addMouseListener(getTableMouseListener());
        table.addKeyListener(getTableKeyListener());
    }

    protected KeyListener getTableKeyListener()
    {
        return new KeyAdapter()
        {
        };
    }
    
    protected MouseListener getTableMouseListener()
    {
        return new MouseAdapter()
        {
        };
    }

    @Override
    protected void initView()
    {
        super.initView();
        
        table = new JTable();

        add(new JScrollPane(table), BorderLayout.CENTER);
        table.setFont(GUI.SONGFONT_FONT);
        pack();
    }
    
    public void updateTable()
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
            table.setModel(t);
            table.updateUI();
        }

        catch (Exception e1)
        {
            GUI.statusText(e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }
    
    public abstract Pair<Object[][], Object[]> toTable() throws FileNotFoundException, IOException;
}
