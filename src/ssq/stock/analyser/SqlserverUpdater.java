package ssq.stock.analyser;

import java.io.File;
import java.io.IOException;

import ssq.stock.DateData;
import ssq.stock.Stock;
import ssq.utils.SqlAccesser;
import ssq.utils.SqlServerAccesser;

public class SqlserverUpdater extends Analyzer
{
    SqlAccesser accesser = new SqlServerAccesser("Stock");
    
    public SqlserverUpdater(String string)
    {
        super(string, Stock.stockFilter);
    }

    public static void main(String[] args) throws Exception
    {
        new SqlserverUpdater("x:/广发证券通达信/").run();
    }
    
    @Override
    public void scan(File f) throws IOException
    {
        Stock stock = new Stock(f, -1, -1);

        for (int i = 0; i < stock.history.size(); i++)
        {
            DateData data = stock.history.get(i);

            try
            {
                accesser.update("INSERT INTO dayline VALUES (?,?,?,?,?,?,?)", new Object[] { stock.number, data.date, data.vals[0], data.vals[1], data.vals[2], data.vals[3], data.scale });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
