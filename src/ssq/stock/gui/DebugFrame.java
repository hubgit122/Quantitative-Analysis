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
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

import ssq.stock.interpreter.Interpreter;

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
        textFields[1].setText(GUI.instance.textFields[3].getText());
    }
    
    private void initView()
    {
        setBackground(Color.WHITE);
        //        setAlwaysOnTop(true);
        setLayout(new GridBagLayout());
        setLocation(750, 450);
        
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
            label.setFont(GUI.SONGFONT_FONT);

            add(label, gbc);

            gbc.gridx = 1;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            add(textFields[i], gbc);
        }
        
        gbc.gridy++;

        gbc.gridx = 0;
        gbc.gridwidth = 2;
        ok.setFont(GUI.SONGFONT_FONT);
        gbc.fill = GridBagConstraints.BOTH;
        add(ok, gbc);

        pack();
        setResizable(false);
    }
    
    private void showResult()
    {
        try
        {
            final Interpreter interpreter = new Interpreter(1, -100f, Integer.valueOf(textFields[1].getText()), GUI.instance.textFields[0].getText(), "tmp", textFields[0].getText());
            interpreter.run();
            
            PipedOutputStream po = new PipedOutputStream();
            PipedInputStream pi = new PipedInputStream();
            final ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(po));
            pi.connect(po);

            new Thread(new Runnable()
            {
                @Override
                public void run()
                {
                    try
                    {
                        o.writeObject(interpreter.AST);
                        o.writeObject(interpreter.evals.get(0));
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    finally
                    {
                        try
                        {
                            o.close();
                        }
                        catch (IOException e)
                        {
                        }
                    }
                }
            }).start();

            new DetailedGradeFrame(pi).show();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
    }

    private void initListeners()
    {
        KeyAdapter tmp = new KeyAdapter()
        {
            @Override
            public void keyPressed(KeyEvent e)
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
