package ssq.stock.interpreter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import ssq.stock.GUI;
import ssq.stock.Stock;
import ssq.stock.interpreter.ReflectTreeBuilder.BiExpression;
import ssq.stock.interpreter.ReflectTreeBuilder.Expression;
import ssq.stock.interpreter.ReflectTreeBuilder.Rule;
import ssq.stock.interpreter.ReflectTreeBuilder.RuleTerm;
import ssq.stock.interpreter.ReflectTreeBuilder.Rules;
import ssq.stock.interpreter.ReflectTreeBuilder.Val;
import ssq.utils.FileUtils;
import ssq.utils.LogUtils;
import ssq.utils.Pair;

public class Interpreter
{
    HashMap<Val, Float>              memory   = new HashMap<>();
    LinkedList<Pair<Integer, Float>> evals    = new LinkedList<>();
    File                             outFile;
    int                              maxInfo;
    float                            minGrade;
    String                           shFilter = "sh600.*|sh601.*|sh603.*";
    String                           szFilter = "sz000.*|sz001.*|sz002.*|sz300.*";

    public static void main(String[] args) throws Exception
    {
        LogUtils.logString(args[0], "选股命令", false);
        LogUtils.logString(args[1], "通达信的安装路径", false);
        LogUtils.logString(args[2], "最大保留结果数", false);
        LogUtils.logString(args[3], "最小保留分数", false);

        new Interpreter(Integer.valueOf(args[2]), Float.valueOf(args[3])).run(args[0], args[1]);
    }

    public Interpreter(Integer max, Float min) throws IOException
    {
        maxInfo = max;
        minGrade = min / 100;
        outFile = new File("result.txt");
    }

    public void run(String insturction, String root) throws Exception
    {
        RuleParser parser = new RuleParser();
        parser.iniParser();
        
        Vector<File> files = FileUtils.getFilteredListOf(new File(root, "vipdoc/sh/lday/"), true, shFilter);
        files.addAll(FileUtils.getFilteredListOf(new File(root, "vipdoc/sz/lday/"), true, szFilter)); //先上海再深圳

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

        Collections.sort(evals, new Comparator<Pair<Integer, Float>>()
                {
            @Override
            public int compare(Pair<Integer, Float> o1, Pair<Integer, Float> o2)
            {
                float v1 = o1.getValue(), v2 = o2.getValue();
                return v1 == v2 ? 0 : v1 > v2 ? -1 : 1;
            }
                });

        if (evals.size() > maxInfo)
        {
            evals = new LinkedList<Pair<Integer, Float>>(evals.subList(0, maxInfo));
        }

        print(evals);
        GUI.statusText("扫描结束, 请去" + outFile + "查看结果");
        LogUtils.logString("扫描结束, 请去" + outFile + "查看结果", "进度信息", false);
    }
    
    protected void scan(Rules AST, File f)
    {
        try
        {
            Stock s = new Stock(f, -1, -1);

            if (s.history.size() == 0)
            {
                throw new Exception("空文件");
            }

            Pair<Integer, Float> result = new Pair<Integer, Float>((s.number << 1) + (s.isShangHai ? 0 : 1), evaluate(s, AST));

            if (result.getValue() > minGrade)
            {
                evals.add(result);
            }
            memory.clear();
        }
        catch (Exception e)
        {
        }
    }
    
    private void print(List<Pair<Integer, Float>> evals)
    {
        BufferedWriter fileWriter;
        try
        {
            fileWriter = new BufferedWriter(new FileWriter(outFile));
        }
        catch (IOException e1)
        {
            e1.printStackTrace();
            GUI.statusText(e1.getLocalizedMessage());
            return;
        }

        try
        {
            for (Pair<Integer, Float> element : evals)
            {
                int nn = element.getKey();
                int num = nn >> 1;
                
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

    private float evaluate(Stock s, Rules AST)
    {
        float result = 1f;
        
        for (Rule rule : AST.rules)
        {
            result *= evaluate(s, rule);

            if (result == 0 || !evals.isEmpty() && result < evals.getLast().getValue())
            {
                break;
            }
        }
        
        return result;
    }

    private float evaluate(Stock s, Rule rule)
    {
        float result = 0f;
        
        for (RuleTerm term : rule.terms)
        {
            result = Math.max(result, evaluate(s, term));
            
            if (result == 1f)
            {
                break;
            }
        }
        
        return result;
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

                    float result = s.history.func(val.func, args);

                    memory.put(val, result);

                    return result;
                }
            }
        }
    }
}
