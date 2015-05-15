package ssq.stock;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;

public class TableFrame extends JFrame
{
    private static final long serialVersionUID = 3146763463910064509L;
    JTable                    table;

    public static void main(String[] args)
    {
        TableFrame frame = new TableFrame();
        
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
    
    public TableFrame()
    {
        initTable();
        initView();
        initListeners();
        show();
    }
    
    private void initListeners()
    {
        table.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int row = table.convertRowIndexToModel(table.getSelectedRow()),
                column = table.convertColumnIndexToModel(0);
                
                String num = table.getModel().getValueAt(row, column).toString();

                String cmd = "sendkey " + num + " " + GUI.instance.textareas[1].getText();

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
        });
    }
    
    private void initView()
    {
        setLayout(new GridBagLayout());
        setBackground(Color.WHITE);
        setResizable(false);
        add(new JScrollPane(table));
        pack();
    }
    
    public void initTable()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File("result.txt")));
            Vector<String[]> data = new Vector<>();
            String[] names = new String[] { "编号", "评分" };
            
            for (String line = reader.readLine(); line != null; line = reader.readLine())
            {
                data.add(line.split(" "));
            }
            reader.close();
            
            table = new JTable(data.toArray(new String[][] {}), names);
            Font yahei = new Font("YaHei", Font.PLAIN, 18);
            table.setFont(yahei);
        }
        catch (Exception e1)
        {
            GUI.statusText(e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }
}
