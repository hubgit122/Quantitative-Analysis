package ssq.stock.gui;

import java.awt.BorderLayout;
import java.awt.Event;
import java.awt.Rectangle;
import java.awt.TextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.table.TableModel;

import ssq.stock.Stock;
import ssq.utils.Pair;

public class QueryResultFrame extends TableFrame
{
    private static final long serialVersionUID = -6907941062744848615L;
    protected TextField       tf               = new TextField(6);

    public QueryResultFrame(File f) throws FileNotFoundException
    {
        super(new FileInputStream(f));

        initView();
        initListeners();
    }
    
    private void initListeners()
    {
        tf.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
            {
                if (e.getKeyCode() == Event.ENTER) //如果检测到输入了Enter键
                {
                    TableModel tm = table.getModel();
                    int cnt = tm.getRowCount();
                    int i = 0;
                    for (; i < cnt; i++)
                    {
                        String tmp = Stock.pad(Integer.valueOf(tm.getValueAt(i, 0).toString()));

                        if (tmp.equals(tf.getText()))
                        {
                            table.setRowSelectionInterval(i, i);

                            Rectangle rect = table.getCellRect(i, 0, true);
                            
                            table.scrollRectToVisible(rect);

                            break;
                        }
                    }
                    
                    if (i == cnt)
                    {
                        JOptionPane.showMessageDialog(null, "未找到当前代码的股票");
                    }
                }
                super.keyReleased(e);
            }
        });
    }
    
    protected void initView()
    {
        JPanel searchJPanel = new JPanel();
        
        JLabel label = new JLabel("搜索股票");
        label.setFont(GUI.yahei);
        searchJPanel.add(label);
        searchJPanel.add(tf);
        
        add(searchJPanel, BorderLayout.NORTH);
    }

    @Override
    public MouseAdapter getTableMouseListener()
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int row = table.convertRowIndexToModel(table.getSelectedRow()), column = table.convertColumnIndexToModel(0);

                String num = table.getModel().getValueAt(row, column).toString();
                
                String cmd = "sendkey " + num + " " + GUI.instance.textFields[1].getText();
                
                try
                {
                    Runtime.getRuntime().exec(cmd);
                }
                catch (IOException e1)
                {
                    GUI.statusText("执行跳转时发生意外: " + e1.getLocalizedMessage());
                    e1.printStackTrace();
                }
            }
        };
    }

    @Override
    public Pair<Object[][], Object[]> toTable() throws FileNotFoundException, IOException
    {
        BufferedReader reader = new BufferedReader(new InputStreamReader(iniData));
        Vector<String[]> data = new Vector<>();
        String[] names = new String[] { "编号", "评分" };
        
        String line = reader.readLine();
        statusLabel.setText("选股命令: " + line);
        
        for (line = reader.readLine(); line != null; line = reader.readLine())
        {
            data.add(line.split(" "));
        }
        reader.close();

        return new Pair<Object[][], Object[]>(data.toArray(new String[][] {}), names);
    }

}
