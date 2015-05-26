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
    
    public static void main(String[] args) throws Exception
    {
        new SqlserverUpdater().run("x:/广发证券通达信/");
    }
    
    @Override
    void scan(File f) throws IOException
    {
        Stock stock = new Stock(f, -1, -1);

        for (int i = 0; i < stock.history.size(); i++)
        {
            DateData data = stock.history.get(i);

            try
            {
                accesser.update("INSERT INTO dayline VALUES (?,?,?,?,?,?,?)", new Object[] { stock.number, data.date, data.opening, data.closing, data.highest, data.lowest, data.scale });
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
