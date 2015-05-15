package ssq.stock;

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

/**
 * 方便操作查询历史的类 查询历史保存在文件中: assets/query_history/日期时间@回溯天数.txt
 *
 * @author s
 */
public class QueryHistory
{
    public static void showHistoryTable()
    {
        final Vector<File> historyFiles = FileUtils.getFilteredListOf(new File(DirUtils.getWritableXxRoot("assets/query_history")), true, ".*");

        new TableFrame()
        {
            private static final long serialVersionUID = 7147103186129623162L;
            
            {
                statusLabel.setText("单击历史以打开结果");
                statusPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
            }

            @Override
            public Pair<Object[][], Object[]> toTable() throws FileNotFoundException, IOException
            {
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

                        new QueryResultFrame(file);
                    }
                };
            }
        };
    }
}
