package ssq.stock.gui;

import java.awt.Adjustable;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.InputStream;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;

/**
 * 有状态条的boarder框
 *
 * @author s
 */
public abstract class FrameWithStatus extends JFrame
{
    protected JLabel      statusLabel = new JLabel();
    protected InputStream iniData;                   //某些超类可能需要使用的初始化输入流
    protected JScrollBar  scrollBar;
    protected JScrollPane statusPane;
    
    public void setStatusText(String s)
    {
        statusLabel.setText(s);
        
        //        if (scrollBar.isVisible())
        //        {
        //            statusPane.setSize(statusPane.getWidth(), 40);
        //        }
    }

    /**
     * 需要初始化数据的超类在构造时把初始化数据的InputStream传入本抽象基类的构造函数, 在超类的toTable方法里按需调用
     *
     * @param is
     */
    public FrameWithStatus(InputStream is)
    {
        this.iniData = is;
        initView();
        initData();
        initListeners();
    }
    
    abstract protected void initData();
    
    abstract protected void initListeners();
    
    @Override
    public void pack()
    {
        setResizable(true);
        super.pack();
        setResizable(false);
    }
    
    protected void initView()
    {
        setBackground(Color.WHITE);
        setLocation(200, 450);
        setLayout(new BorderLayout());
        statusLabel.setFont(GUI.SONGFONT_FONT);
        statusPane = new JScrollPane(statusLabel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        scrollBar = new JScrollBar(Adjustable.HORIZONTAL);
        statusPane.setHorizontalScrollBar(scrollBar);
        statusPane.setWheelScrollingEnabled(true);
        
        JPanel statusAndScroll = new JPanel(new BorderLayout());
        statusAndScroll.add(statusPane, BorderLayout.CENTER);
        statusAndScroll.add(scrollBar, BorderLayout.SOUTH);
        
        add(statusAndScroll, BorderLayout.SOUTH);
        pack();
        setResizable(false);
    }
}
