package ssq.stock.analyser;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;

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
                             protected void tryCreateTable()
                             {
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
                             protected void tryCreateDB()
                             {
                             }
                             
                             @Override
                             public void updateDatabase()
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
        final String tableName = stock.getNumberString() + stock.name;
        int lastDate = -1;
        ResultSet tmp = null;
        try
        {
            tmp = accesser.query("select max(日期) from [" + tableName + "]", null);
            tmp.next();
            lastDate = tmp.getInt(1);
        }
        catch (Exception e1)
        {
            e1.printStackTrace();
        }
        finally
        {
            try
            {
                tmp.getStatement().close();
            }
            catch (SQLException e)
            {
            }
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
