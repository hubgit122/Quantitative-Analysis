package ssq.stock.analyser;

import java.io.IOException;

import ssq.stock.DateData;
import ssq.stock.Stock;
import ssq.utils.SqlAccesser;
import ssq.utils.SqlServerAccesser;

public class SqlserverUpdater extends Analyzer
{
    SqlAccesser accesser = new SqlServerAccesser("Stock");

    public static void main(String[] args) throws Exception
    {
        new SqlserverUpdater().run();
    }
    
    @Override
    public void scan(Stock stock) throws IOException
    {
        for (int i = 0; i < stock.history.size(); i++)
        {
            DateData data = stock.history.get(i);

            try
            {
                accesser.update("INSERT INTO dayline VALUES (?,?,?,?,?,?,?)", new Object[] { stock.getNumber(), stock.name, data.date, data.getVal(0), data.getVal(1), data.getVal(2), data.getVal(3), data.getVal(4), data.getVal(5), data.getVal(6) });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
