package ssq.stock.interpreter;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import ssq.stock.Stock;
import ssq.stock.analyser.Analyzer;
import ssq.stock.gui.GUI;
import ssq.stock.gui.RecordHistory;
import ssq.stock.interpreter.ReflectTreeBuilder.BiExpression;
import ssq.stock.interpreter.ReflectTreeBuilder.BiRuleExpression;
import ssq.stock.interpreter.ReflectTreeBuilder.BinaryRuleOperator;
import ssq.stock.interpreter.ReflectTreeBuilder.Expression;
import ssq.stock.interpreter.ReflectTreeBuilder.RuleExpression;
import ssq.stock.interpreter.ReflectTreeBuilder.RuleVal;
import ssq.stock.interpreter.ReflectTreeBuilder.Val;
import ssq.utils.DirUtils;
import ssq.utils.LogUtils;
import ssq.utils.Pair;
import ssq.utils.TreeNode;

public class Interpreter extends Analyzer
{
    HashMap<Expression, Float> memory   = new HashMap<>();
    
    File                       outFile;
    int                        maxInfo;
    int                        backDays = 0;
    float                      minGrade;
    public RuleExpression      AST      = null;
    String                     instruction;
    String                     outputDir;

    public static RuleParser   parser   = new RuleParser();
    static
    {
        try
        {
            parser.iniParser();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /**
     * 初始化选股器
     *
     * @param max
     * @param min
     * @param days
     * @throws IOException
     */
    public Interpreter(Integer max, Float min, Integer days, String insturction, String root) throws IOException
    {
        this(max, min, days, insturction, root, "assets/query_history", Stock.stockFilter);
    }
    
    /**
     * 初始化规定了输出文件和股票代码过滤器的选股器
     *
     * @param max
     * @param min
     * @param days
     * @param outDir
     * @param filterString
     * @throws IOException
     */
    public Interpreter(Integer max, Float min, Integer days, String insturction, String root, String outDir, String filter) throws IOException
    {
        super(root, filter);
        this.outputDir = outDir;
        maxInfo = max;
        minGrade = min / 100;
        backDays = days;
        this.instruction = insturction;
        AST = parser.getRoot(instruction);
    }
    
    @Override
    public void run() throws Exception
    {
        evals.clear();
        
        super.run();

        if (evals.size() > maxInfo)
        {
            evals = new Evaluations(evals.subList(0, maxInfo));
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");

        outFile = new File(DirUtils.getXxRoot(outputDir), simpleDateFormat.format(new Date()) + "@" + backDays);

        print();
        GUI.statusText("扫描结束, 请点击按钮查看结果");
        LogUtils.logString("扫描结束, 请点击按钮查看结果", "进度信息", false);
    }

    @Override
    public void scan(File file)
    {
        try
        {
            Stock s = new Stock(file, -1, -1);

            if (s.history.size() == 0)
            {
                throw new Exception("空文件");
            }

            TreeNode<Float> result = evaluate(s, AST);

            if (result.getElement() >= minGrade)
            {
                evals.add(new Pair<Integer, TreeNode<Float>>(s.number, result));
            }
            memory.clear();
        }
        catch (Exception e)
        {
        }
    }
    
    private TreeNode<Float> evaluate(Stock s, RuleExpression AST)
    {
        float grade;
        TreeNode<Float> result;
        
        if (AST instanceof BiRuleExpression)
        {
            BiRuleExpression expr = (BiRuleExpression) AST;
            
            TreeNode<Float> lresult = evaluate(s, expr.lrule);
            
            TreeNode<Float> rresult = evaluate(s, expr.rrule);
            
            grade = expr.op == BinaryRuleOperator.AND ? lresult.getElement() * rresult.getElement() : Math.max(lresult.getElement(), rresult.getElement());
            result = new TreeNode<Float>(grade);

            result.addChildNode(lresult);
            result.addChildNode(rresult);
        }
        else
        {
            RuleVal val = (RuleVal) AST;

            float lExp = evaluate(s, val.lexpr), rExp = evaluate(s, val.rexpr);
            int order = val.inequality.ordinal();

            if (order < 2) // < or <=
            {
                grade = saturate(rExp / lExp);
            }
            else if (order > 2)
            {
                grade = saturate(lExp / rExp);
            }
            else
            {
                grade = Math.min(rExp / lExp, lExp / rExp);
            }

            result = new TreeNode<Float>(grade);
            result.addChild(lExp);
            result.addChild(rExp);
        }

        return result;
    }

    private static float saturate(float f)
    {
        if (f > 1f)
        {
            return 1f;
        }
        else if (f < 0f)
        {
            return 0f;
        }
        else
        {
            return f;
        }
    }

    private float evaluate(Stock s, Expression expr)
    {
        if (expr instanceof BiExpression)
        {
            BiExpression biExpr = (BiExpression) expr;
            return biExpr.operator.doOp(evaluate(s, biExpr.lExpression), evaluate(s, biExpr.rExpression));
        }
        else
        { // Val
            Val val = (Val) expr;

            if (val.isFloat)
            {
                return ((Val) expr).val;
            }
            else
            {
                Float f = memory.get(val);

                if (f != null)
                {
                    return f;
                }
                else
                {
                    ArrayList<Float> args = new ArrayList<>();

                    for (Expression e : val.args)
                    {
                        args.add(evaluate(s, e));
                    }

                    args.add((float) backDays);
                    
                    float result = s.history.func(val.func, args, val.type, val.rest);

                    memory.put(val, result);

                    return result;
                }
            }
        }
    }

    private void print() throws IOException
    {
        ObjectOutputStream o = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile)));
        try
        {
            o.writeObject(new RecordHistory(AST, evals));
        }
        catch (IOException e)
        {
            e.printStackTrace();
            GUI.statusText(e.getLocalizedMessage());
        }

        try
        {
            o.close();
        }
        catch (IOException e)
        {
        }
    }

    public static class Evaluations extends LinkedList<Pair<Integer, TreeNode<Float>>> implements Serializable
    {
        private static final long serialVersionUID = 1L;

        public Evaluations(List<Pair<Integer, TreeNode<Float>>> subList)
        {
            super(subList);
        }

        public Evaluations()
        {
        }
        
        @Override
        public boolean add(Pair<Integer, TreeNode<Float>> e)
        {

            if (this.size() == 0)
            {
                addFirst(e);
            }
            if (e.getValue().getElement() > this.get(0).getValue().getElement())
            {
                addFirst(e);
                return true;
            }

            for (ListIterator<Pair<Integer, TreeNode<Float>>> iterator = listIterator(); iterator.hasNext();)
            {
                Pair<Integer, TreeNode<Float>> node = iterator.next();

                if (e.getValue().getElement() > node.getValue().getElement())
                {
                    iterator.previous();
                    iterator.add(e);
                    return true;
                }
            }

            addLast(e);
            return true;
        };
    }

    public Evaluations evals = new Evaluations();
}
