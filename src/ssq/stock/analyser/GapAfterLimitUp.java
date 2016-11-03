package ssq.stock.analyser;

import java.io.File;
import java.io.FileWriter;

import ssq.stock.DateData;
import ssq.stock.Stock;
import ssq.stock.analyser.ReflectTreeBuilder.ValueType;

public class GapAfterLimitUp extends Analyzer
{
	public static void main(String[] args) throws NumberFormatException, Exception
	{
		new GapAfterLimitUp().run();
	}
	
	FileWriter writer;
	
	public GapAfterLimitUp() throws Exception
	{
	}
	
	@Override
	public void run() throws Exception
	{
		writer = new FileWriter(new File("out.txt"));
		writer.write("股票代码,缺口十字星日期,最高涨幅,收盘涨幅\n");
		super.run();
		writer.close();
	}
	
	@Override
	public void scan(Stock s)
	{
		try
		{
			if (s.history.isEmpty())
			{
				System.err.println(s + " 没有记录");
			}
			
			for (int i = getNextStar(s, 0); i > 0 && i < s.history.size() - 1; i = getNextStar(s, i))
			{
				DateData thisDay = s.history.get(i), latterDay = s.history.get(i + 1);
				double highestRise = (latterDay.getScaledVal(ValueType.highest) / thisDay.getScaledVal(ValueType.closing) - 1) * 100;
				double closingRise = (latterDay.getScaledVal(ValueType.closing) / thisDay.getScaledVal(ValueType.closing) - 1) * 100;
					
				if (highestRise>11)
				{
					System.err.println();
				}
				
				writer.write(s.getCodeString() + "," + thisDay.date + "," + highestRise + "," + closingRise + "\n");
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private int getNextStar(Stock s, int lastIndex)
	{
		for (int i = lastIndex + 2; i < s.history.size(); ++i)
		{
			DateData formerDay = s.history.get(i - 2), lastDay = s.history.get(i - 1), thisDay = s.history.get(i);
			
			if (thisDay.getScaledVal(ValueType.lowest) > lastDay.getScaledVal(ValueType.highest) && lastDay.getScaledVal(ValueType.highest) == lastDay.getScaledVal(ValueType.closing) && lastDay.getScaledVal(ValueType.highest) >= formerDay.getScaledVal(ValueType.closing) * 1.099 && lastDay.getScaledVal(ValueType.closing) * 1.1 > thisDay.getScaledVal(ValueType.closing))
			{
				return i;
			}
		}
		
		return 0;
	}
}
