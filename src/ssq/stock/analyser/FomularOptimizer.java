package ssq.stock.analyser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import ssq.stock.Stock;
import ssq.stock.interpreter.Interpreter;
import ssq.stock.interpreter.ReflectTreeBuilder.RuleLevel;
import ssq.utils.Pair;

public class FomularOptimizer extends Analyzer
{
    private String    fomular;
    private LinkedList<Pair<Integer, Integer>> preferedList = new LinkedList<>(), notPreferedList = new LinkedList<>();
    
    public FomularOptimizer(String fomular)
    {
        this.fomular = fomular;
        
        iniLists();
    }

    private void iniLists()
    {
        BufferedReader fin = null;
        try
        {
            fin = new BufferedReader(new FileReader(new File("prefered.txt")));
            for (String line = fin.readLine(); line != null; line = fin.readLine())
            {
                String[] parts = line.split(" ");
                preferedList.add(new Pair<Integer, Integer>(Integer.valueOf(parts[0]), Integer.valueOf(parts[1])));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                fin.close();
            }
            catch (IOException e)
            {
            }
        }

        try
        {
            fin = new BufferedReader(new FileReader(new File("not_prefered.txt")));
            for (String line = fin.readLine(); line != null; line = fin.readLine())
            {
                String[] parts = line.split(" ");
                notPreferedList.add(new Pair<Integer, Integer>(Integer.valueOf(parts[0]), Integer.valueOf(parts[1])));
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                fin.close();
            }
            catch (IOException e)
            {
            }
        }
    }
    
    @Override
    public void run() throws Exception
    {
        Interpreter.parser.getRoot(fomular);
        
        super.run();
    }

    @Override
    public void scan(Stock stock) throws IOException
    {
        // TODO 自动生成的方法存根

    }
}
