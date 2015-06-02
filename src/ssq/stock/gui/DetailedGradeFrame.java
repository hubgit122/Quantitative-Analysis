package ssq.stock.gui;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;

import ssq.stock.interpreter.ReflectTreeBuilder.BiRuleExpression;
import ssq.stock.interpreter.ReflectTreeBuilder.RuleExpression;
import ssq.utils.Pair;
import ssq.utils.TreeNode;

public class DetailedGradeFrame extends TreeFrame
{
    RuleExpression                 AST;
    Pair<Integer, TreeNode<Float>> record;
    
    public DetailedGradeFrame(InputStream is)
    {
        super(is);
    }

    @Override
    protected void initView()
    {
        super.initView();
    }

    @Override
    protected TreeModel toTree()
    {
        try
        {
            ObjectInputStream i = new ObjectInputStream(new BufferedInputStream(iniData));
            AST = (RuleExpression) i.readObject();
            record = (Pair<Integer, TreeNode<Float>>) i.readObject();
            i.close();

            statusLabel.setText(" 公式: " + AST.toString());
            setTitle("股票: " + record.getKey() + " 总分: " + String.valueOf(record.getValue().getElement() * 100));
            
            return new DefaultTreeModel(getTreeRecursively(AST, record.getValue()));
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }

    private DefaultMutableTreeNode getTreeRecursively(RuleExpression expr, TreeNode<Float> node)
    {
        DefaultMutableTreeNode result = new DefaultMutableTreeNode(expr.toString() + ":" + node.getElement() * 100);

        if (expr instanceof BiRuleExpression)
        {
            BiRuleExpression biRuleExpression = (BiRuleExpression) expr;
            result.add(getTreeRecursively(biRuleExpression.lrule, node.getChildList().get(0)));
            result.add(getTreeRecursively(biRuleExpression.rrule, node.getChildList().get(1)));
        }
        
        return result;
    }
}
