package ssq.stock;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

import ssq.utils.Pair;

public class QueryResultFrame extends TableFrame
{
    private static final long serialVersionUID = -6907941062744848615L;
    
    QueryResultFrame(File f)
    {
        super(f);
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
        };
    }

    @Override
    public Pair<Object[][], Object[]> toTable() throws FileNotFoundException, IOException
    {
        BufferedReader reader = new BufferedReader(new FileReader(file));
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
