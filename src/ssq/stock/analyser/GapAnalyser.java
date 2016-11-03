package ssq.stock.analyser;

import java.io.File;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import ssq.stock.DateData;
import ssq.stock.Stock;
import ssq.stock.analyser.ReflectTreeBuilder.ValueType;

public class GapAnalyser extends Analyzer
{
	public static void main(String[] args) throws NumberFormatException, Exception
	{
		new GapAnalyser(Integer.valueOf(args[0])).run();
	}
	
	final int			n;
	final List<Integer>	list	= new LinkedList<>();
	FileWriter			writer;
						
	public GapAnalyser(int n) throws Exception
	{
		this.n = n;
		
		Scanner scanner = new Scanner(new File("Table.txt"));
		
		while (scanner.hasNextLine())
		{
			String line = scanner.nextLine();
			
			list.add(Integer.valueOf(line.substring(0, 6)));
		}
		
		scanner.close();
	}
	
	@Override
	public void run() throws Exception
	{
		writer = new FileWriter(new File("out.txt"));
		writer.write("股票, a日, b日, c日, d日, 缺口上缘, 缺口上缘复权, 缺口下缘, 缺口下缘复权, 缺口大小复权, 缺口比重\n");
		super.run();
		writer.close();
	}
	
	@Override
	public void scan(Stock s)
	{
		try
		{
			if (s.getCode()==600035)
			{
				System.err.println();
			}
			if (s.history.isEmpty())
			{
				System.err.println(s + " 没有记录");
			}
			
			if (!list.contains(s.getCode()))
			{
				System.err.println("跳过 " + s);
				return;
			}
			
			for (int i = getNextDownwardGapStart(s, 0); i > 0;)
			{
				DateData formerDay = s.history.get(i - 1), latterDay = s.history.get(i);
				
				float lastGapUpperBound = formerDay.getScaledVal(ValueType.lowest);
				float lastGapSize = formerDay.getScaledVal(ValueType.lowest) - latterDay.getScaledVal(ValueType.highest);
				float gapWeight = lastGapSize / lastGapUpperBound * 100;
				
				int nextDownwardGapStart = getNextDownwardGapStart(s, i);
				
				for (int j = i + 1; j < (nextDownwardGapStart > 0 ? nextDownwardGapStart : s.history.size()); ++j)
				{
					DateData thisDay = s.history.get(j);
					if (thisDay.getScaledVal(ValueType.highest) >= lastGapUpperBound)
					{
						DateData lastDay = s.history.get(j - 1);
						if (thisDay.getScaledVal(ValueType.lowest) >= lastGapUpperBound && lastDay.getScaledVal(ValueType.highest) < lastGapUpperBound && j - i >= n)
						{
							writer.write(s.toString() + ", " + formerDay.date + ", " + latterDay.date + ", " + lastDay.date + ", "  + thisDay.date + ", " + formerDay.getVal(ValueType.lowest) + ", " + formerDay.getScaledVal(ValueType.lowest) + ", " + latterDay.getVal(ValueType.highest) + ", " + latterDay.getScaledVal(ValueType.highest) + ", " + lastGapSize + ", " + gapWeight + "\n");
						}
						else
						{
							break;
						}
					}
				}
				
				i = nextDownwardGapStart;
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	private int getNextDownwardGapStart(Stock s, int lastIndex)
	{
		for (int i = lastIndex + 1; i < s.history.size(); ++i)
		{
			DateData formerDay = s.history.get(i - 1), latterDay = s.history.get(i);
			if (formerDay.getScaledVal(ValueType.lowest) > latterDay.getScaledVal(ValueType.highest))
			{
				return i;
			}
		}
		
		return 0;
	}
}
