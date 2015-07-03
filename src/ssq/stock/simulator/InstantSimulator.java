package ssq.stock.simulator;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Date;

import ssq.stock.Stock;
import ssq.utils.FileUtils;

public class InstantSimulator extends Simulator
{
    public InstantSimulator(String name) throws Exception
    {
        super(name);
    }

    private static final long serialVersionUID = 1L;
    
    @Override
    public Float getCurrentPrice(int code) throws IOException
    {
        return new Stock(null, code).getCurrentPrice();
    }
    
    @Override
    public boolean commit(Account account, int code, float price, boolean buy, int quantity)
    {
        Stock stock = new Stock(null, code);

        if (buy && price*quantity<account.available || !buy && !account.canSell(stock, price, quantity, new Date()))
        {
            return false;
        }
        
        account.commissionList.add(new Commission(, price, quantity, buy, new Date()));
        return account;
    }
    
    @Override
    public void save()
    {
        try
        {
            ObjectOutputStream oo = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(FileUtils.assertFileExists(new File(simDir, name)))));
            oo.writeObject(this);
            oo.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

    }
}
