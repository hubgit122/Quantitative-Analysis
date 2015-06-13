package ssq.stock.gui;

import java.io.Serializable;
import java.util.LinkedList;

import ssq.stock.interpreter.ReflectTreeBuilder.RuleLevel;
import ssq.utils.Pair;
import ssq.utils.TreeNode;

public class RecordHistory implements Serializable
{
    private static final long                  serialVersionUID = 1L;
    
    RuleLevel                             AST;
    LinkedList<Pair<Integer, TreeNode<Float>>> records;
    
    public RecordHistory(RuleLevel insturction, LinkedList<Pair<Integer, TreeNode<Float>>> records)
    {
        this.AST = insturction;
        this.records = records;
    }
}
