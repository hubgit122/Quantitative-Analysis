package ssq.stock.interpreter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Vector;

import ssq.stock.Stock;
import ssq.stock.gui.GUI;
import ssq.stock.interpreter.ReflectTreeBuilder.BiExpression;
import ssq.stock.interpreter.ReflectTreeBuilder.Expression;
import ssq.stock.interpreter.ReflectTreeBuilder.Rule;
import ssq.stock.interpreter.ReflectTreeBuilder.RuleTerm;
import ssq.stock.interpreter.ReflectTreeBuilder.Rules;
import ssq.stock.interpreter.ReflectTreeBuilder.Val;
import ssq.utils.DirUtils;
import ssq.utils.FileUtils;
import ssq.utils.LogUtils;
import ssq.utils.Pair;

public class Interpreter
{
    HashMap<Val, Float> memory   = new HashMap<>();
    
    File                outFile;
    int                 maxInfo;
    int                 backDays = 0;
    float               minGrade;
    
    static RuleParser   parser   = new RuleParser();
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

    public Interpreter(Integer max, Float min, Integer days) throws IOException
    {
        maxInfo = max;
        minGrade = min / 100;
        backDays = days;
    }

    public void run(String insturction, String root) throws Exception
    {
        run(insturction, root, "assets/query_history", Stock.stockFilter);
    }

    public void run(String insturction, String root, String outputDir, String filter) throws Exception
    {
        evals.clear();
        filter = filter != null ? filter : Stock.stockFilter;

        Vector<File> files = FileUtils.getFilteredListOf(new File(root, "vipdoc/sh/lday/"), true, filter);
        files.addAll(FileUtils.getFilteredListOf(new File(root, "vipdoc/sz/lday/"), true, filter)); //先上海再深圳

        GUI.statusText("开始分析");
        LogUtils.logString("开始分析", "进度信息", false);

        int i = 0;

        for (File f : files)
        {
            scan(parser.getRoot(insturction), f);

            if (++i % 100 == 0) //每扫描1000支可能的股票更新显示
            {
                GUI.statusText("扫描总数: " + i + ", 扫描百分比: " + (100.0 * i / files.size()));
                LogUtils.logString("扫描总数: " + i + ", 扫描百分比: " + (100.0 * i / files.size()), "进度信息", false);
            }
        }

        if (evals.size() > maxInfo)
        {
            evals = new LinkedList<Pair<Integer, Float>>(evals.subList(0, maxInfo));
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");

        outFile = new File(DirUtils.getXxRoot(outputDir), simpleDateFormat.format(new Date()) + "@" + backDays);

        print(insturction, evals);
        GUI.statusText("扫描结束, 请点击按钮查看结果");
        LogUtils.logString("扫描结束, 请点击按钮查看结果", "进度信息", false);
    }
    
    /**
     * 求编号为number的股票的原子得分情况
     *
     * @param insturction
     * @param root
     * @param number
     * @return
     */
    public Pair<Float, Vector<Vector<Float>>> scan(String insturction, String root, int back, int number)
    {
        String filter = "s." + number + ".*";
        this.backDays = back;

        try
        {
            return scan(insturction, root, filter).get(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return null;
        }
    }
    
    /**
     * 求满足filter条件的股票的原子得分情况
     *
     * @param insturction
     * @param root
     * @param number
     * @return
     */
    //      股票                                   或公式      与公式    原子值
    public Vector<Pair<Float, Vector<Vector<Float>>>> scan(String insturction, String root, String filter)
    {
        evals.clear();
        Vector<File> files = FileUtils.getFilteredListOf(new File(root, "vipdoc/sh/lday/"), true, filter);
        files.addAll(FileUtils.getFilteredListOf(new File(root, "vipdoc/sz/lday/"), true, filter)); //先上海再深圳
        Vector<Pair<Float, Vector<Vector<Float>>>> result = new Vector<>();

        for (File f : files)
        {
            result.add(scan(parser.getRoot(insturction), f));
        }

        return result;
    }

    /**
     * 扫描当前文件, 求所代表的股票的原子评分
     *
     * @param AST
     * @param file
     * @return
     */
    protected Pair<Float, Vector<Vector<Float>>> scan(Rules AST, File file)
    {
        try
        {
            Stock s = new Stock(file, -1, -1);

            if (s.history.size() == 0)
            {
                throw new Exception("空文件");
            }

            Pair<Float, Vector<Vector<Float>>> result = evaluate(s, AST);

            if (result.getKey() > minGrade)
            {
                evals.add(new Pair<Integer, Float>(s.number, result.getKey()));
            }
            memory.clear();
            
            return result;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 扫描当前股票, 求所代表的股票的原子评分
     *
     * @param s
     * @param AST
     * @return
     */
    private Pair<Float, Vector<Vector<Float>>> evaluate(Stock s, Rules AST)
    {
        Vector<Vector<Float>> result = new Vector<>();
        float grade = Float.MIN_VALUE;
        
        for (Rule rule : AST.rules)
        {
            Pair<Float, Vector<Float>> tmp = evaluate(s, rule);
            result.add(tmp.getValue());
            grade = Math.max(grade, tmp.getKey());

            if (grade == 1)
            {
                break;
            }
        }
        
        return new Pair<Float, Vector<Vector<Float>>>(grade, result);
    }

    private Pair<Float, Vector<Float>> evaluate(Stock s, Rule rule)
    {
        Vector<Float> result = new Vector<>();
        float grade = 1f;
        
        for (RuleTerm term : rule.terms)
        {
            float tmp = evaluate(s, term);
            result.add(tmp);
            grade *= tmp;
            
            if (grade == 0 || !evals.isEmpty() && grade < evals.getLast().getValue())
            {
                break;
            }
        }

        return new Pair<Float, Vector<Float>>(grade, result);
    }

    private float evaluate(Stock s, RuleTerm term)
    {
        float lExp = evaluate(s, term.lexpr), rExp = evaluate(s, term.rexpr);
        int order = term.inequality.ordinal();

        if (order < 2) // < or <=
        {
            return saturate(rExp / lExp);
        }
        else if (order > 2)
        {
            return saturate(lExp / rExp);
        }
        else
        {
            return Math.min(rExp / lExp, lExp / rExp);
        }

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
                    Vector<Float> args = new Vector<>();

                    for (Expression e : val.args)
                    {
                        args.add(evaluate(s, e));
                    }

                    args.add((float) backDays);

                    float result = s.history.func(val.func, args);

                    memory.put(val, result);

                    return result;
                }
            }
        }
    }

    private void print(String insturction, List<Pair<Integer, Float>> evals) throws IOException
    {
        BufferedWriter fileWriter = new BufferedWriter(new FileWriter(outFile));

        fileWriter.write(insturction);
        fileWriter.write("\r\n");

        try
        {
            for (Pair<Integer, Float> element : evals)
            {
                int num = element.getKey();
                
                fileWriter.write(Stock.pad(num) + ' ' + element.getValue() * 100 + "\r\n");
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
            GUI.statusText(e.getLocalizedMessage());
        }

        try
        {
            fileWriter.close();
        }
        catch (IOException e)
        {
        }
    }

    LinkedList<Pair<Integer, Float>> evals = new LinkedList<Pair<Integer, Float>>()
                                           {
                                               private static final long serialVersionUID = 944050349742239541L;
                                               
                                               @Override
                                               public boolean add(ssq.utils.Pair<Integer, Float> e)
                                               {
                                                   if (this.size() == 0)
                                                   {
                                                       addFirst(e);
                                                   }
                                                   if (e.getValue() > this.get(0).getValue())
                                                   {
                                                       addFirst(e);
                                                       return true;
                                                   }
                                                   
                                                   for (ListIterator<Pair<Integer, Float>> iterator = evals.listIterator(); iterator.hasNext();)
                                                   {
                                                       Pair<Integer, Float> pair = iterator.next();
                                                       
                                                       if (e.getValue() > pair.getValue())
                                                       {
                                                           iterator.previous();
                                                           iterator.add(e);
                                                           return true;
                                                       }
                                                   }
                                                   
                                                   addLast(e);
                                                   return true;
                                               };
                                           };
}
