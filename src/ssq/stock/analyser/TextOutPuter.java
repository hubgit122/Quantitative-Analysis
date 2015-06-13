package ssq.stock.analyser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import ssq.stock.DateData;
import ssq.stock.Stock;

public class TextOutPuter extends Analyzer
{
    public static void main(String[] args) throws Exception
    {
        new TextOutPuter("tmp").run();
    }
    
    File outDir;

    public TextOutPuter(String outDir)
    {
        this.outDir = new File(outDir);
        this.outDir.mkdirs();
    }
    
    @Override
    public void scan(Stock stock) throws IOException
    {
        FileWriter writer = new FileWriter(new File(outDir.getAbsolutePath(), stock.toString().replace('*', '@')));
        
        for (DateData data : stock.history)
        {
            writer.write(data.toString().replace('[', ',').replace("]", "") + "\r\n");
        }
        writer.close();
    }
}
