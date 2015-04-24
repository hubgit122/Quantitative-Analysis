package ssq.stock;

import java.io.IOException;
import java.util.Arrays;

import org.junit.BeforeClass;
import org.junit.Test;

public class StockTest
{
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
    }
    
    @Test
    public void test() throws IOException
    {
        Stock s = new Stock(false, 300129);
        
        System.out.println(s.history.func("max", Arrays.asList(250.0f, 1.0f)));
        return;
    }
}
