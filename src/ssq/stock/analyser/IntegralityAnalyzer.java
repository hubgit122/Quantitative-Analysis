package ssq.stock.analyser;

import java.io.IOException;
import java.util.Date;

import ssq.stock.DateData;
import ssq.stock.Stock;

public class IntegralityAnalyzer extends Analyzer
{
	public static void main(String[] args) throws Exception
	{
		new IntegralityAnalyzer().run();
	}
	
	@Override
	public void scan(Stock stock) throws IOException
	{
		if (stock.history.isEmpty())
		{
			return;
		}
		
		Date lastDate = DateData.numberToDate(stock.history.get(0).date);
		
		for (int i = 1; i < stock.history.size(); ++i)
		{
			Date thisDate = DateData.numberToDate(stock.history.get(i).date);
			
			if (thisDate.getTime() - lastDate.getTime() > 1000l * 60l * 60l * 24l * 80l)
			{
				System.out.println(stock + ": " + DateData.format.format(lastDate) + " " + DateData.format.format(thisDate));
			}
			
			lastDate = thisDate;
		}
	}
}
