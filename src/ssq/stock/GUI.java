package ssq.stock;

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

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ssq.stock.interpreter.Interpreter;
import ssq.utils.DirUtils;

public class GUI extends JFrame
{
    private static final String PREF_TXT         = "pref.txt";

    private static final String READY            = "查看参数是否正确, 并按开始";

    private static final long   serialVersionUID = 1L;
    
    public static GUI           instance         = null;
    public JLabel               statusLabel      = new JLabel(READY);
    JLabel[]                    labels           = new JLabel[] { new JLabel("选股公式"), new JLabel("通达信路径"), new JLabel("最大列表长度"), new JLabel("最小接受的分值"), new JLabel("回溯天数") };
    protected TextField[]       textareas        = new TextField[] { new TextField(90), new TextField(90), new TextField(90), new TextField(90), new TextField(90) };
    JButton[]                   buttons          = new JButton[] { new JButton("保存设置"), new JButton("恢复默认设置"), new JButton("显示结果"), new JButton("开始选股") };
    String[]                    hints            = new String[] { "保存设置到文件", "恢复成默认设置", "显示最近一次的选股结果", "开始分析" };
    
    public static void main(String[] args)
    {
        new GUI();
    }
    
    public static void statusText(String s)
    {
        if (GUI.instance != null)
        {
            GUI.instance.statusLabel.setText(s);
        }
    }

    public GUI()
    {
        super("选股");
        iniView();
        iniListeners();
        iniData();
        instance = this;
        setVisible(true);
    }
    
    private void iniData()
    {
        try
        {
            BufferedReader reader = new BufferedReader(new FileReader(new File(DirUtils.getXxRoot("assets"), PREF_TXT)));
            for (int i = 0; i < textareas.length; i++)
            {
                textareas[i].setText(reader.readLine());
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
    
    protected void iniListeners()
    {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        for (int i = 0; i < buttons.length; i++)
        {
            buttons[i].addMouseListener(listeners[i]);
        }
    }
    
    private MouseListener[] listeners = new MouseListener[] {
                                      new MouseAdapter()
                                      {
                                          @Override
                                          public void mouseEntered(MouseEvent e)
                                          {
                                              statusLabel.setText(hints[0]);
                                          }
                                          
                                          @Override
                                          public void mouseExited(MouseEvent e)
                                          {
                                              statusLabel.setText(READY);
                                          }
                                          
                                          @Override
                                          public void mouseClicked(MouseEvent e)
                                          {
                                              try
                                              {
                                                  FileWriter fileWriter = new FileWriter(new File(DirUtils.getXxRoot("assets"), PREF_TXT));
                                                  
                                                  for (int i = 0; i < textareas.length; i++)
                                                  {
                                                      fileWriter.write(textareas[i].getText() + "\r\n");
                                                  }
                                                  
                                                  fileWriter.close();
                                              }
                                              catch (IOException e1)
                                              {
                                                  e1.printStackTrace();
                                                  statusLabel.setText(e1.getLocalizedMessage());
                                              }
                                          }
                                      },
                                      new MouseAdapter()
                                      {
                                          @Override
                                          public void mouseEntered(MouseEvent e)
                                          {
                                              statusLabel.setText(hints[1]);
                                          }
                                          
                                          @Override
                                          public void mouseExited(MouseEvent e)
                                          {
                                              statusLabel.setText(READY);
                                          }
                                          
                                          @Override
                                          public void mouseClicked(MouseEvent e)
                                          {
                                              restoreData();
                                          }
                                          
                                      },
                                      new MouseAdapter()
                                      {
                                          @Override
                                          public void mouseClicked(MouseEvent e)
                                          {
                                              QueryHistory.showHistoryTable();
                                          }
                                          
                                          @Override
                                          public void mouseEntered(MouseEvent e)
                                          {
                                              statusLabel.setText(hints[2]);
                                          }
                                          
                                          @Override
                                          public void mouseExited(MouseEvent e)
                                          {
                                              statusLabel.setText(READY);
                                          }
                                      },
                                      new MouseAdapter()
                                      {
                                          @Override
                                          public void mouseEntered(MouseEvent e)
                                          {
                                              statusLabel.setText(hints[3]);
                                          }
                                          
                                          @Override
                                          public void mouseExited(MouseEvent e)
                                          {
                                              statusLabel.setText(READY);
                                          }
                                          
                                          @Override
                                          public void mouseClicked(MouseEvent e)
                                          {
                                              disableButtons();
                                              
                                              new Thread(new Runnable()
                                              {
                                                  @Override
                                                  public void run()
                                                  {
                                                      try
                                                      {
                                                          Interpreter.main(new String[] { textareas[0].getText(), textareas[1].getText(), textareas[2].getText(), textareas[3].getText(), textareas[4].getText() });
                                                      }
                                                      catch (Exception e1)
                                                      {
                                                          e1.printStackTrace();
                                                          statusLabel.setText("执行出现异常" + e1.getLocalizedMessage());
                                                      }
                                                      
                                                      GUI.this.enableButtons();
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
        
        iniListeners();
    }
    
    private void restoreData()
    {
        textareas[0].setText("min(350 ->1) < min(500 -> 351) && max(5 -> 1) > max(300 -> 6) && max(250 -> 1) < min(250 -> 1)  * 1.5 && max(250->30) *1.1 >  max(1->1)");
        textareas[1].setText("d:/股票");
        textareas[2].setText(String.valueOf(100));
        textareas[3].setText(String.valueOf(50));
        textareas[4].setText(String.valueOf(0));
    }
    
    private void deleteListeners()
    {
        for (int i = 0; i < buttons.length; i++)
        {
            buttons[i].removeMouseListener(listeners[i]);
        }
    }
    
    protected void iniView()
    {
        setBackground(Color.WHITE);
        setResizable(true);
        setLayout(new GridBagLayout());

        Font yahei = new Font("YaHei", Font.PLAIN, 14);
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
            label.setFont(yahei);
            
            add(label, gbc);
            
            gbc.gridx = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            add(textareas[i], gbc);
        }

        gbc.gridy++;
        for (int i = 0; i < buttons.length; i++)
        {
            JButton button = buttons[i];

            gbc.gridx = i;
            gbc.gridwidth = 1;
            button.setFont(yahei);
            if (i == 3)
            {
                gbc.anchor = GridBagConstraints.EAST;
            }
            add(button, gbc);
        }
        
        gbc.gridy++;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.gridx = 0;
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        add(statusLabel, gbc);

        pack();
    }
}