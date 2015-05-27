package ssq.stock.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Vector;

import javax.swing.ScrollPaneConstants;

import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import ssq.utils.Pair;

public class QueryHistoryFrame extends TableFrame
{
    public static QueryHistoryFrame instance = null;

    public static void showQueryHistory()
    {
        if (instance == null)
        {
            instance = new QueryHistoryFrame();
        }
        else
        {
            instance.updateTable();
        }
        instance.show();
    }

    public QueryHistoryFrame()
    {
        statusLabel.setText("单击历史以打开结果");
        statusPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    }
    
    @Override
    public Pair<Object[][], Object[]> toTable() throws FileNotFoundException, IOException
    {
        final Vector<File> historyFiles = FileUtils.getFilteredListOf(new File(DirUtils.getWritableXxRoot("assets/query_history")), true, ".*");
        Vector<String[]> data = new Vector<>();
        String[] names = new String[] { "时间", "回溯天数" };
        
        for (File file : historyFiles)
        {
            data.add(file.getName().split("\\.")[0].split("@"));
        }
        
        return new Pair<Object[][], Object[]>(data.toArray(new String[][] {}), names);
    }
    
    @Override
    protected MouseListener getTableMouseListener()
    {
        return new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                int row = table.convertRowIndexToModel(table.getSelectedRow());
                int column = table.convertColumnIndexToModel(0);
                
                String time = table.getModel().getValueAt(row, column).toString();

                File file = FileUtils.getFilteredListOf(new File(DirUtils.getWritableXxRoot("assets/query_history")), true, time + ".*").get(0);
                
                try
                {
                    QueryResultFrame tmp = new QueryResultFrame(file);
                    tmp.setAlwaysOnTop(true);
                    tmp.show();
                }
                catch (FileNotFoundException e1)
                {
                    GUI.statusText(e1.getLocalizedMessage());
                    e1.printStackTrace();
                }
            }
        };
    }
}
