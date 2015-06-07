package ssq.stock.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import ssq.stock.interpreter.Interpreter;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;

public class GUI extends FrameWithStatus
{
    private static final String READY            = "查看参数是否正确, 并按开始";
    
    private static final long   serialVersionUID = 1L;

    public static GUI           instance         = null;
    JLabel[]                    labels;
    protected TextField[]       textFields;
    JButton[]                   buttons;
    public static final Font    SONGFONT_FONT    = new Font("宋体", Font.PLAIN, 16);

    public static void main(String[] args)
    {
        new GUI();
    }

    public static void statusText(String s)
    {
        if (GUI.instance != null)
        {
            GUI.instance.setStatusText(s);
        }
    }
    
    public GUI()
    {
        super(null);
        instance = this;
        setVisible(true);
    }

    @Override
    protected void initData()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(DirUtils.getXxRoot("assets"), "pref.txt")));
            for (int i = 0; i < textFields.length; i++)
            {
                textFields[i].setText(reader.readLine());
            }

            try
            {
                reader.close();
            }
            catch (IOException e)
            {
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            
            restoreData();
        }
    }

    @Override
    protected void initListeners()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        for (int i = 0; i < buttons.length; i++)
        {
            buttons[i].addMouseListener(listeners[i]);
        }
    }
    
    public static GUI getInstance()
    {
        return instance;
    }
    
    static private MouseListener[] listeners = new MouseListener[] {
                                             new MouseAdapter()
                                             {
                                                 @Override
                                                 public void mouseEntered(MouseEvent e)
                                                 {
                                                     statusText("保存设置到文件");
                                                 }
                                                 
                                                 @Override
                                                 public void mouseExited(MouseEvent e)
                                                 {
                                                     statusText(READY);
                                                 }
                                                 
                                                 @Override
                                                 public void mouseClicked(MouseEvent e)
                                                 {
                                                     try
                                                     {
                                                         File src = new File(DirUtils.getXxRoot("assets"), "pref.txt");
                                                         
                                                         FileWriter fileWriter = new FileWriter(src);
                                                         for (int i = 0; i < GUI.getInstance().textFields.length; i++)
                                                         {
                                                             fileWriter.write(GUI.getInstance().textFields[i].getText() + "\r\n");
                                                         }
                                                         
                                                         fileWriter.close();
                                                         
                                                         File dst = new File(DirUtils.getXxRoot("assets"), "pref" + Calendar.getInstance().getTimeInMillis() + ".txt");
                                                         FileUtils.copyFile(src, dst);
                                                     }
                                                     catch (IOException e1)
                                                     {
                                                         e1.printStackTrace();
                                                         statusText(e1.getLocalizedMessage());
                                                     }
                                                 }
                                             },
                                             new MouseAdapter()
                                             {
                                                 @Override
                                                 public void mouseEntered(MouseEvent e)
                                                 {
                                                     statusText("恢复成默认设置");
                                                 }
                                                 
                                                 @Override
                                                 public void mouseExited(MouseEvent e)
                                                 {
                                                     statusText(READY);
                                                 }
                                                 
                                                 @Override
                                                 public void mouseClicked(MouseEvent e)
                                                 {
                                                     GUI.getInstance().restoreData();
                                                 }
                                             },
                                             new MouseAdapter()
                                             {
                                                 @Override
                                                 public void mouseClicked(MouseEvent e)
                                                 {
                                                     QueryHistoryFrame.showQueryHistory();
                                                 }
                                                 
                                                 @Override
                                                 public void mouseEntered(MouseEvent e)
                                                 {
                                                     statusText("显示选股历史");
                                                 }
                                                 
                                                 @Override
                                                 public void mouseExited(MouseEvent e)
                                                 {
                                                     statusText(READY);
                                                 }
                                             },
                                             new MouseAdapter()
                                             {
                                                 @Override
                                                 public void mouseClicked(MouseEvent e)
                                                 {
                                                     new DebugFrame();
                                                 }
                                                 
                                                 @Override
                                                 public void mouseEntered(MouseEvent e)
                                                 {
                                                     statusText("打开一个调试窗口, 调试各原子公式");
                                                 }
                                                 
                                                 @Override
                                                 public void mouseExited(MouseEvent e)
                                                 {
                                                     statusText(READY);
                                                 }
                                             },
                                             new MouseAdapter()
                                             {
                                                 @Override
                                                 public void mouseEntered(MouseEvent e)
                                                 {
                                                     statusText("开始分析");
                                                 }
                                                 
                                                 @Override
                                                 public void mouseExited(MouseEvent e)
                                                 {
                                                     statusText(READY);
                                                 }
                                                 
                                                 @Override
                                                 public void mouseClicked(MouseEvent e)
                                                 {
                                                     GUI.getInstance().disableButtons();
                                                     
                                                     new Thread(new Runnable()
                                                     {
                                                         @Override
                                                         public void run()
                                                         {
                                                             try
                                                             {
                                                                 new Interpreter(Integer.valueOf(GUI.getInstance().textFields[1].getText()), Float.valueOf(GUI.getInstance().textFields[2].getText()), Integer.valueOf(GUI.getInstance().textFields[3].getText()), GUI.getInstance().textFields[0].getText()).run();
                                                             }
                                                             catch (Exception e1)
                                                             {
                                                                 e1.printStackTrace();
                                                                 statusText("执行出现异常" + e1.getLocalizedMessage());
                                                             }
                                                             
                                                             GUI.getInstance().enableButtons();
                                                             
                                                             QueryHistoryFrame.showQueryHistory();
                                                         }
                                                     }).start();
                                                 }
                                             }
                                             };
    
    private void disableButtons()
    {
        for (int i = 0; i < buttons.length; i++)
        {
            JButton jButton = buttons[i];
            jButton.setEnabled(false);
        }
        
        deleteListeners();
    }
    
    private void enableButtons()
    {
        for (int i = 0; i < buttons.length; i++)
        {
            JButton jButton = buttons[i];
            jButton.setEnabled(true);
        }

        initListeners();
    }

    private void restoreData()
    {
        textFields[0].setText("min(350 ->1) < min(500 -> 351) && max(5 -> 1) > max(300 -> 6) && max(250 -> 1) < min(250 -> 1)  * 1.5 && max(250->30) *1.1 >  max(1->1)");
        textFields[1].setText("d:/股票");
        textFields[2].setText(String.valueOf(100));
        textFields[3].setText(String.valueOf(50));
        textFields[4].setText(String.valueOf(0));
    }

    private void deleteListeners()
    {
        for (int i = 0; i < buttons.length; i++)
        {
            buttons[i].removeMouseListener(listeners[i]);
        }
    }

    @Override
    protected void initView()
    {
        super.initView();
        
        labels = new JLabel[] { new JLabel("选股公式"), new JLabel("最大列表长度"), new JLabel("最小接受的分值"), new JLabel("回溯天数") };
        textFields = new TextField[] { new TextField(90), new TextField(90), new TextField(90), new TextField(90) };
        buttons = new JButton[] { new JButton("保存设置"), new JButton("恢复默认设置"), new JButton("显示结果"), new JButton("调试选股公式"), new JButton("开始选股") };

        setTitle("选股");
        setBackground(Color.WHITE);
        setLocation(200, 200);

        JPanel main = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridheight = 1;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(1, 1, 1, 1);
        gbc.ipadx = 1;
        gbc.ipady = 1;
        for (int i = 0; i < labels.length; i++)
        {
            gbc.gridy++;
            
            gbc.gridx = 0;
            gbc.gridwidth = 1;
            JLabel label = labels[i];
            label.setFont(SONGFONT_FONT);

            main.add(label, gbc);

            gbc.gridx = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            main.add(textFields[i], gbc);
        }
        
        gbc.gridy++;
        for (int i = 0; i < buttons.length; i++)
        {
            JButton button = buttons[i];
            
            gbc.gridx = i;
            gbc.gridwidth = 1;
            button.setFont(SONGFONT_FONT);
            if (i == buttons.length - 1)
            {
                gbc.anchor = GridBagConstraints.EAST;
            }
            main.add(button, gbc);
        }

        add(main, BorderLayout.CENTER);

        setStatusText(READY);
        pack();
    }
}