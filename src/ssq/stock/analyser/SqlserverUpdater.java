package ssq.stock.analyser;

import java.io.IOException;

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
    };
    
    @Override
    public void run() throws Exception
    {
        accesser.checkDatabase(null);
        super.run();
    }
    
    @Override
    public void scan(final Stock stock) throws IOException
    {
        for (int i = 0; i < stock.history.size(); i++)
        {
            DateData data = stock.history.get(i);
            final String tableName = stock.getNumberString() + stock.name;

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
