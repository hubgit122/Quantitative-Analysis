package ssq.stock.analyser;

import java.io.IOException;
import java.sql.ResultSet;

import javax.swing.JOptionPane;

import ssq.stock.DateData;
import ssq.stock.IDNamePair;
import ssq.stock.Stock;
import ssq.utils.FileUtils;
import ssq.utils.SqlAccesser;
import ssq.utils.SqlServerAccesser;

public class SqlserverUpdater extends Analyzer
{
    String createTable = FileUtils.openAssetsString("CreateStockTable.sql");

    public static void main(String[] args) throws Exception
    {
        new SqlserverUpdater().run();
    }
    
    SqlAccesser accesser = new SqlServerAccesser("Stock")
                         {
                             @Override
                             protected void tryInitializeDB()
                             {
                                 if (connection == null)
                                 {
                                     JOptionPane.showMessageDialog(null, "请从兼容sqlserver2005的数据库管理软件中建立数据库\"Stock\", 并将用户\"sa\"的密码设置为\"00\", 并打开数据库服务. ");
                                     System.exit(-1);
                                 }
                                 
                                 for (IDNamePair stock : Stock.stockList)
                                 {
                                     String sql = FileUtils.openAssetsString("CreateStockTable.sql").replace("?", stock.toString());
                                     try
                                     {
                                         update(sql, new Object[] {});
                                     }
                                     catch (Exception e)
                                     {
                                     }
                                 }
                             }
                             
                             @Override
                             public void updateDB()
                             {
                             }
                             
                             @Override
                             public String getVersion()
                             {
                                 return "1.0";
                             }
                         };

    @Override
    public void run() throws Exception
    {
        accesser.checkDatabase();
        super.run();
    }

    @Override
    public void scan(final Stock stock) throws IOException
    {
        final String tableName = stock.getCodeString() + stock.name;
        int lastDate = -1;
        try
        {
            ResultSet tmp = accesser.query("select max(日期) from [" + tableName + "]", null);
            tmp.next();
            lastDate = tmp.getInt(1);
            tmp.getStatement().close();
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }

        for (int i = 0; i < stock.history.size(); i++)
        {
            DateData data = stock.history.get(i);
            if (data.date > lastDate)
            {
                try
                {
                    accesser.update("INSERT INTO [" + tableName + "] VALUES (?,?,?,?,?,?,?,?)", new Object[] { data.date, data.getVal(0), data.getVal(1), data.getVal(2), data.getVal(3), data.getVal(4), data.getVal(5), data.getVal(6) });
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
    }
}
