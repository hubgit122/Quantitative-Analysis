package ssq.stock.gui;

import java.awt.Color;
import java.awt.Event;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.TextField;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import org.apache.commons.lang.StringUtils;

import ssq.stock.interpreter.Interpreter;
import ssq.utils.Pair;

public class DebugFrame extends JFrame
{
    private static final long serialVersionUID = 1L;
    JLabel[]                  labels           = new JLabel[] { new JLabel("股票代码"), new JLabel("回溯天数") };
    TextField[]               textFields       = new TextField[] { new TextField(6), new TextField(6) };
    JButton                   ok               = new JButton("开始分项评分");
    
    public DebugFrame()
    {
        initData();
        initView();
        initListeners();
        show();
    }

    private void initData()
    {
        textFields[1].setText(GUI.instance.textFields[4].getText());
    }

    private void initView()
    {
        setBackground(Color.WHITE);
        //        setAlwaysOnTop(true);
        setLayout(new GridBagLayout());

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
            label.setFont(GUI.yahei);
            
            add(label, gbc);
            
            gbc.gridx = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            add(textFields[i], gbc);
        }

        gbc.gridy++;
        
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        ok.setFont(GUI.yahei);
        gbc.fill = GridBagConstraints.BOTH;
        add(ok, gbc);
        
        pack();
        setResizable(false);
    }

    private void showResult()
    {
        String fomular = GUI.instance.textFields[0].getText();
        String[] fomulars = fomular.split("\\|\\|");

        for (int i = 0; i < fomulars.length; i++)
        {
            final String fomu = fomulars[i];

            new TableFrame()
            {
                private static final long serialVersionUID = 1L;

                {
                    statusLabel.setText(" 公式: " + fomu);

                    try
                    {
                        setTitle("股票: " + textFields[0].getText() + "总分: " + String.valueOf(new Interpreter(1, 0.f, Integer.valueOf(textFields[1].getText())).scan(fomu, GUI.instance.textFields[1].getText(), Integer.valueOf(textFields[1].getText()), Integer.valueOf(textFields[0].getText())).getKey() * 100));
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                
                @Override
                public Pair<Object[][], Object[]> toTable() throws IOException
                {
                    String[] atomFomular = fomu.split("&&");

                    Vector<String[]> data = new Vector<>();
                    String[] names = new String[] { "原子公式", "得分" };
                    
                    for (int j = 0; j < atomFomular.length; j++)
                    {
                        String string = StringUtils.trim(atomFomular[j]);

                        try
                        {
                            data.add(new String[] { string, String.valueOf(new Interpreter(1, 0.f, Integer.valueOf(textFields[1].getText())).scan(string, GUI.instance.textFields[1].getText(), Integer.valueOf(textFields[1].getText()), Integer.valueOf(textFields[0].getText())).getKey() * 100) });
                        }
                        catch (Exception e)
                        {
                            statusLabel.setText(e.getLocalizedMessage());
                            e.printStackTrace();
                        }
                    }
                    
                    return new Pair<Object[][], Object[]>(data.toArray(new String[][] {}), names);
                }

                @Override
                protected MouseListener getTableMouseListener()
                {
                    return new MouseAdapter()
                    {
                    };
                }
            }.show();
        }
    }

    private void initListeners()
    {
        KeyAdapter tmp = new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (e.getKeyCode() == Event.ENTER) //如果检测到输入了Enter键
                    showResult();
                super.keyReleased(e);
            }
        };

        for (TextField tf : textFields)
        {
            tf.addKeyListener(tmp);
        }

        ok.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                showResult();
            }
        });
    }
}
