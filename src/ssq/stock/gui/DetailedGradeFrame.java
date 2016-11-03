package ssq.stock.gui;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import ssq.stock.Stock;
import ssq.stock.analyser.ReflectTreeBuilder.CompositeRule;
import ssq.stock.analyser.ReflectTreeBuilder.RuleLevel;
import ssq.utils.Pair;
import ssq.utils.TreeNode;

public class DetailedGradeFrame extends TreeFrame
{
    RuleLevel                      AST;
    Pair<Integer, TreeNode<Float>> record;
    
    public DetailedGradeFrame(InputStream is)
    {
        super(is);
        switchExpand(tree.getPathForRow(0), true);
    }

    @Override
    protected void initComponent()
    {
        super.initComponent();
    }

    @Override
    protected TreeModel toTree()
    {
        try
        {
            ObjectInputStream i = new ObjectInputStream(new BufferedInputStream(iniData));
            AST = (RuleLevel) i.readObject();
            record = (Pair<Integer, TreeNode<Float>>) i.readObject();
            i.close();

            statusLabel.setText(" 公式: " + AST.toString());
            setTitle("股票: " + Stock.pad(record.getKey()) + " 总分: " + String.valueOf(record.getValue().getElement() * 100));
            
            return new DefaultTreeModel(getTreeRecursively(AST, record.getValue()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private DefaultMutableTreeNode getTreeRecursively(RuleLevel expr, TreeNode<Float> node)
    {
        DefaultMutableTreeNode result = new DefaultMutableTreeNode(node.getElement() * 100 + "      " + expr.toString());

        if (expr instanceof CompositeRule)
        {
            int i = 0;
            CompositeRule composite = (CompositeRule) expr;
            for (RuleLevel ruleLevel : composite.rules)
            {
                result.add(getTreeRecursively(ruleLevel, node.getChildList().get(i++)));
            }
        }
        
        return result;
    }
}
