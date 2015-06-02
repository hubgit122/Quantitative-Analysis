package ssq.stock.gui;

import java.awt.BorderLayout;
import java.awt.event.KeyListener;
import java.awt.event.MouseListener;
import java.io.InputStream;

import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.TreeModel;

public abstract class TreeFrame extends FrameWithStatus
{
    JTree tree;
    
    public TreeFrame(InputStream is)
    {
        super(is);
    }

    @Override
    protected void initView()
    {
        super.initView();
        
        tree = new JTree();
        tree.setFont(GUI.SONGFONT_FONT);
        JScrollPane pane = new JScrollPane(tree);
        add(pane, BorderLayout.CENTER);
        setSize(500, 200);
        setLocation(700, 450);
        setAlwaysOnTop(true);
        setResizable(true);
    }

    @Override
    protected void initData()
    {
        updateTree();
    }
    
    private void updateTree()
    {
        try
        {
            tree.setModel(toTree());
            tree.updateUI();
        }

        catch (Exception e1)
        {
            GUI.statusText(e1.getLocalizedMessage());
            e1.printStackTrace();
        }
    }
    
    /**
     * 得到构成JTree所需的对象树.
     *
     * @return List嵌套构成的树. 每个节点的第0个子节点是该节点的字符串,
     */
    abstract protected TreeModel toTree();

    @Override
    protected void initListeners()
    {
        tree.addMouseListener(getTreeMouseListener());
        tree.addKeyListener(getTreeKeyListener());
    }
    
    private KeyListener getTreeKeyListener()
    {
        return null;
    }
    
    private MouseListener getTreeMouseListener()
    {
        return null;
    }
}
