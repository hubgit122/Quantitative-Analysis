package ssq.stock.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.block.BlockContainer;
import org.jfree.chart.block.BorderArrangement;
import org.jfree.chart.labels.StandardXYToolTipGenerator;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.CandlestickRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;

public class TestApplet extends FrameWithStatus
{
    List ls  = new ArrayList(); //定义一个用来保存数据的集合类List

    Map  map = null;           //用来表示一条记录

    public static void main(String[] args)
    {
        new TestApplet();
    }
    
    public TestApplet()
    {
        super(null);
        JPanel jpanel = createDemoPanel();
        add(jpanel, BorderLayout.CENTER);
        super.pack();
        show();
    }

    public void clearList()
    {//清空保存数据的集合类List
        ls.clear();
    }

    private JFreeChart createCombinedChart()
    {
        Map m = createDatasetMap();//从数据对象里取出各种类型的对象，主要是用来表示均线的时间线(IntervalXYDataset)对象和用来表示阴阳线和成交量的蜡烛图对象(OHLCDataset)
        IntervalXYDataset avg_line5 = (IntervalXYDataset) m.get("avg_line5");
        IntervalXYDataset avg_line10 = (IntervalXYDataset) m.get("avg_line10");
        IntervalXYDataset avg_line20 = (IntervalXYDataset) m.get("avg_line20");
        IntervalXYDataset avg_line60 = (IntervalXYDataset) m.get("avg_line60");
        IntervalXYDataset vol_avg_line5 = (IntervalXYDataset) m.get("vol_avg_line5");
        IntervalXYDataset vol_avg_line10 = (IntervalXYDataset) m.get("vol_avg_line10");
        OHLCDataset k_line = (OHLCDataset) m.get("k_line");
        OHLCDataset vol = (OHLCDataset) m.get("vol");
        String stock_name = (String) m.get("stock_name");

        //设置若干个时间线的Render，目的是用来让几条均线显示不同的颜色，并为时间线加上鼠标提示
        XYLineAndShapeRenderer xyLineRender = new XYLineAndShapeRenderer(true, false);
        xyLineRender.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("yyyy-MM-dd"), new DecimalFormat("0.00")));
        xyLineRender.setSeriesPaint(0, Color.red);
        
        XYLineAndShapeRenderer xyLineRender1 = new XYLineAndShapeRenderer(true, false);
        xyLineRender1.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("yyyy-MM-dd"), new DecimalFormat("0.00")));
        xyLineRender1.setSeriesPaint(0, Color.BLACK);

        XYLineAndShapeRenderer xyLineRender2 = new XYLineAndShapeRenderer(true, false);
        xyLineRender1.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("yyyy-MM-dd"), new DecimalFormat("0.00")));
        xyLineRender1.setSeriesPaint(0, Color.blue);

        XYLineAndShapeRenderer xyLineRender3 = new XYLineAndShapeRenderer(true, false);
        xyLineRender1.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("yyyy-MM-dd"), new DecimalFormat("0.00")));
        xyLineRender1.setSeriesPaint(0, Color.darkGray);

        //定义K线图上半截显示的Plot
        XYPlot volPlot = new XYPlot(vol_avg_line5, null, new NumberAxis("股票价格情况"), xyLineRender);

        //定义一个CandlestickRenderer给蜡烛图对象使用，目的是对蜡烛图对象的显示进行调整，这里主要是调整它显示的宽度并加上鼠标提示
        CandlestickRenderer candlesRender = new CandlestickRenderer();
        candlesRender.setCandleWidth(4D);
        candlesRender.setBaseToolTipGenerator(new StandardXYToolTipGenerator("{0}: ({1}, {2})", new SimpleDateFormat("yyyy-MM-dd"), new DecimalFormat("0.00")));

        //把其它的几个Dataset加到上半截要显示的Plot里，并同时设置它们所采用的Render，以形成一个叠加显示的效果
        volPlot.setDataset(1, vol_avg_line10);
        volPlot.setRenderer(1, xyLineRender1);
        volPlot.setDataset(2, vol);
        volPlot.setRenderer(2, candlesRender);
        XYPlot candlePlot = new XYPlot(k_line, null, new NumberAxis(""), candlesRender);
        candlePlot.setDataset(1, avg_line5);
        candlePlot.setRenderer(1, xyLineRender1);
        candlePlot.setDataset(2, avg_line10);
        candlePlot.setRenderer(2, xyLineRender2);
        candlePlot.setDataset(3, avg_line20);
        candlePlot.setRenderer(3, xyLineRender3);
        candlePlot.setDataset(4, avg_line60);
        candlePlot.setRenderer(4, xyLineRender);
        DateAxis dateaxis = new DateAxis("K线图");
        dateaxis.setTickUnit(new DateTickUnit(1, 1, new SimpleDateFormat("MM/yy")));//设置日期格式及显示日期的间隔
        dateaxis.setLowerMargin(0.0D);
        dateaxis.setUpperMargin(0.02D);

        //定义一个复合类型的Plot，目的是为了把Chart的上半截和下半截结合起来，形成一张完整的K线图
        CombinedDomainXYPlot combineXY = new CombinedDomainXYPlot(dateaxis);
        //把上下两个Plot都加到复合Plot里，并设置它们在图中所占的比重
        combineXY.add(candlePlot, 3);
        combineXY.add(volPlot, 1);
        combineXY.setGap(8D);
        combineXY.setDomainGridlinesVisible(true);
        JFreeChart jfreechart = new JFreeChart(stock_name,
                JFreeChart.DEFAULT_TITLE_FONT, combineXY, false);
        jfreechart.setBackgroundPaint(Color.white);

        //为Chart图添加一个图例，这里我们可以定义需要显示的一些信息，及图例放置的位置，我们选择的顶部
        LegendTitle legendtitle = new LegendTitle(candlePlot);
        BlockContainer blockcontainer = new BlockContainer(new BorderArrangement());
        blockcontainer.setFrame(new BlockBorder(0.10000000000000001D, 0.10000000000000001D, 0.10000000000000001D, 0.10000000000000001D));
        BlockContainer blockcontainer1 = legendtitle.getItemContainer();
        blockcontainer1.setPadding(2D, 10D, 5D, 2D);
        blockcontainer.add(blockcontainer1);
        legendtitle.setWrapper(blockcontainer);
        legendtitle.setPosition(RectangleEdge.TOP);
        legendtitle.setHorizontalAlignment(HorizontalAlignment.CENTER);
        jfreechart.addSubtitle(legendtitle);
        return jfreechart;
    }

    ///该方法主要是为WEB端的JS调用,定义一个新的Map,插入一行空的记录
    public void insertRecord()
    {
        map = new HashMap();
    }

    ///该方法主要是为WEB端的JS调用,为当前记录设置值
    public void setValue(String key, String value)
    {
        map.put(key, value);
    }

    ///该方法主要是为WEB端的JS调用,把当前记录添加到记录集List里
    public void postRecord()
    {
        ls.add(map);
    }

    public Map createDatasetMap()
    {

        //从每一行记录里取出特定值，用来开成各种类型的均线和阴阳线图
        Map m = new HashMap();
        TimeSeries avg_lin5 = new TimeSeries("5日均线",
                org.jfree.data.time.Day.class);
        TimeSeries avg_lin10 = new TimeSeries("10日均线",
                org.jfree.data.time.Day.class);
        TimeSeries avg_lin20 = new TimeSeries("20日均线",
                org.jfree.data.time.Day.class);
        TimeSeries avg_lin60 = new TimeSeries("60日均线",
                org.jfree.data.time.Day.class);
        TimeSeries vol_avg_lin5 = new TimeSeries(
                "5日成交量均线",
                org.jfree.data.time.Day.class);
        TimeSeries vol_avg_lin10 = new TimeSeries(
                "10日成交量均线",
                org.jfree.data.time.Day.class);

        int count = ls.size();
        Date adate[] = new Date[count];
        double high[] = new double[count];
        double low[] = new double[count];
        double close[] = new double[count];
        double open[] = new double[count];
        double volume[] = new double[count];
        Date adate1[] = new Date[count];
        double high1[] = new double[count];
        double low1[] = new double[count];
        double close1[] = new double[count];
        double open1[] = new double[count];
        double volume1[] = new double[count];

        String stock_name = null;
        Calendar cal = Calendar.getInstance();

        for (int j = 0; j < ls.size(); j++)
        {
            Map vMap = (Map) ls.get(j);

            stock_name = (String) vMap.get("stock_name");
            Date issue_date = new Date(Long.parseLong(vMap.get("issue_date")
                    .toString()));
            double open_value = Double.parseDouble(vMap.get("open_value")
                    .toString());
            double high_value = Double.parseDouble(vMap.get("high_value")
                    .toString());
            double low_value = Double.parseDouble(vMap.get("low_value")
                    .toString());
            double close_value = Double.parseDouble(vMap.get("close_value")
                    .toString());
            double avg5 = Double.parseDouble(vMap.get("avg5").toString());
            double avg10 = Double.parseDouble(vMap.get("avg10").toString());
            double avg20 = Double.parseDouble(vMap.get("avg20").toString());
            double avg60 = Double.parseDouble(vMap.get("avg60").toString());
            double volume_value = Double.parseDouble(vMap.get("volume_value")
                    .toString());
            double vol_avg5 = Double.parseDouble(vMap.get("vol_avg5")
                    .toString());
            double vol_avg10 = Double.parseDouble(vMap.get("vol_avg10")
                    .toString());
            cal.setTime(issue_date);

            if (avg5 > 0.0D)
                avg_lin5.addOrUpdate(new Day(cal.get(5), cal.get(2) + 1, cal.get(1)),
                        avg5);
            if (avg10 > 0.0D)
                avg_lin10.addOrUpdate(new Day(cal.get(5), cal.get(2) + 1, cal.get(1)),
                        avg10);
            if (avg20 > 0.0D)
                avg_lin20.addOrUpdate(new Day(cal.get(5), cal.get(2) + 1, cal.get(1)),
                        avg20);
            if (avg60 > 0.0D)
                avg_lin60.addOrUpdate(new Day(cal.get(5), cal.get(2) + 1, cal.get(1)),
                        avg60);
            if (vol_avg5 > 0.0D)
                vol_avg_lin5.addOrUpdate(
                        new Day(cal.get(5), cal.get(2) + 1, cal.get(1)),
                        vol_avg5);
            if (vol_avg10 > 0.0D)
                vol_avg_lin10.addOrUpdate(new Day(cal.get(5), cal.get(2) + 1, cal
                        .get(1)), vol_avg10);
            adate[j] = issue_date;
            high[j] = high_value;
            low[j] = low_value;
            close[j] = close_value;
            open[j] = open_value;
            volume[j] = 0.0D;
            adate1[j] = issue_date;
            high1[j] = 0.0D;
            low1[j] = 0.0D;
            close1[j] = 0.0D;
            open1[j] = 0.0D;

            //这里是我们用蜡烛图来构造与阴阳线对应的成交量图，我们主要是通过判断开盘价与收盘价相比较的值来决定到底是在表示成交量的蜡烛图的开盘价设置值还是收盘价设置值，设置之前我们把它们全部都设置为0
            if (open_value < close_value)
                close1[j] = volume_value;
            else
                open1[j] = volume_value;
            volume1[j] = 0.0D;
        }
        DefaultHighLowDataset k_line = new DefaultHighLowDataset("",
                adate, high, low, close, open, volume);
        DefaultHighLowDataset vol = new DefaultHighLowDataset(
                "", adate1, high1, low1, close1, open1,
                volume1);
        //把各种类型的图表对象放到Map里，以为其它方法提供使用
        m.put("k_line", k_line);
        m.put("vol", vol);
        m.put("stock_name", stock_name);
        m.put("avg_line5", new TimeSeriesCollection(avg_lin5));
        m.put("avg_line10", new TimeSeriesCollection(avg_lin10));
        m.put("avg_line20", new TimeSeriesCollection(avg_lin20));
        m.put("avg_line60", new TimeSeriesCollection(avg_lin60));
        m.put("vol_avg_line5", new TimeSeriesCollection(vol_avg_lin5));
        m.put("vol_avg_line10", new TimeSeriesCollection(vol_avg_lin10));

        return m;
    }

    //该方法主要是为WEB端的JS调用，用来动态改变K线图
    public void changeApplet()
    {
        Map m = createDatasetMap();
        IntervalXYDataset avg_line5 = (IntervalXYDataset) m.get("avg_line5");
        IntervalXYDataset avg_line10 = (IntervalXYDataset) m.get("avg_line10");
        IntervalXYDataset avg_line20 = (IntervalXYDataset) m.get("avg_line20");
        IntervalXYDataset avg_line60 = (IntervalXYDataset) m.get("avg_line60");
        IntervalXYDataset vol_avg_line5 = (IntervalXYDataset) m
                .get("vol_avg_line5");
        IntervalXYDataset vol_avg_line10 = (IntervalXYDataset) m
                .get("vol_avg_line10");
        OHLCDataset k_line = (OHLCDataset) m.get("k_line");
        OHLCDataset vol = (OHLCDataset) m.get("vol");

        ChartPanel panel = (ChartPanel) getContentPane();
        JFreeChart chart = panel.getChart();
        CombinedDomainXYPlot plot = (CombinedDomainXYPlot) chart.getPlot();
        List list = plot.getSubplots();
        XYPlot candlePlot = (XYPlot) list.get(0);
        candlePlot.setDataset(0, k_line);
        candlePlot.setDataset(1, avg_line5);
        candlePlot.setDataset(2, avg_line10);
        candlePlot.setDataset(3, avg_line20);
        candlePlot.setDataset(4, avg_line60);

        XYPlot volPlot = (XYPlot) list.get(1);
        volPlot.setDataset(0, vol);
        volPlot.setDataset(1, vol_avg_line5);
        volPlot.setDataset(2, vol);
        volPlot.setDataset(3, vol_avg_line10);

        repaint();
    }

    public JPanel createDemoPanel()
    {
        JFreeChart jfreechart = createCombinedChart();
        return new ChartPanel(jfreechart);
    }

    @Override
    protected void initData()
    {
        
    }

    @Override
    protected void initListeners()
    {
        
    }
}